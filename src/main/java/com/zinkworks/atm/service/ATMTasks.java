package com.zinkworks.atm.service;

import com.zinkworks.atm.dto.BalanceDetails;
import com.zinkworks.atm.dto.OperationDetails;
import com.zinkworks.atm.entity.ATM;
import com.zinkworks.atm.entity.Account;
import com.zinkworks.atm.exceptions.ATMException;
import com.zinkworks.atm.exceptions.IncorrectAccountInformationException;
import com.zinkworks.atm.exceptions.NoSufficientFundsException;
import com.zinkworks.atm.repository.ATMRepository;
import com.zinkworks.atm.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

@Service
@AllArgsConstructor
public class ATMTasks {

    private AccountRepository accountRepository;
    private ATMRepository atmRepository;

    public BalanceDetails getBalance(String accountNumber, String pin) {
        final Account account = getAccount(accountNumber, pin);
        return new BalanceDetails(account.getBalance(), account.getOverdraft());
    }

    public OperationDetails withdrawnMoney(String accountNumber, String pin, int amountToWithdraw) {
        final Account account = getAccount(accountNumber, pin);
        //We know that there is an ATM object with id = 1, because we initialized it with data.sql file
        final ATM atm = atmRepository.findById(1L).get();

        if (amountToWithdraw < 5) {
            throw new ATMException("Cannot withdraw amount below 5$");
        }

        if (atm.getATMBalance() <= 0) {
            throw new ATMException("ATM run out of funds!");
        }

        if (atm.getATMBalance() < amountToWithdraw) {
            throw new ATMException("ATM does not have sufficient funds!");
        }

        if (hasSufficientFunds(account, amountToWithdraw)) {
            final OperationDetails operationDetails = withdrawMoney(atm, amountToWithdraw);

            if (operationDetails.getAmountWithdrawn() != 0) {
                throw new ATMException("Could not withdraw money!");
            }

            account.setBalance(account.getBalance() - amountToWithdraw);
            atmRepository.save(atm);
            accountRepository.save(account);
            operationDetails.setAmountWithdrawn(amountToWithdraw);
            return operationDetails;
        } else {
                throw new NoSufficientFundsException();
        }
    }

    private OperationDetails withdrawMoney(ATM atm, int amountToWithdraw) {
        Map<Integer, Integer> notesAmountMap = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<Integer, Integer> entry : atm.getDenominationAmountMap().entrySet()) {
            final Integer denomination = entry.getKey();
            final Integer amountOfBills = entry.getValue();
            int amountOfBillsNeeded = amountToWithdraw / denomination;
            if (amountOfBillsNeeded > 0) {
                if (amountOfBillsNeeded <= amountOfBills) {
                    amountToWithdraw -= amountOfBillsNeeded * denomination;
                    atm.getDenominationAmountMap().put(denomination, amountOfBills - amountOfBillsNeeded);
                    notesAmountMap.put(denomination, amountOfBillsNeeded);
                } else {
                    int billsMissing = amountOfBillsNeeded - amountOfBills;
                    int billsWithdrawnCount = amountOfBillsNeeded - billsMissing;
                    amountToWithdraw -= billsWithdrawnCount * denomination;
                    atm.getDenominationAmountMap().put(denomination, amountOfBills - billsWithdrawnCount);
                    notesAmountMap.put(denomination, billsWithdrawnCount);
                }
            }
        }
        return new OperationDetails(amountToWithdraw, notesAmountMap);
    }

    private boolean hasSufficientFunds(Account account, int amountToWithdraw) {
        return  (account.getBalance() + account.getOverdraft() - amountToWithdraw >= 0);
    }

    private Account getAccount(String accountNumber, String pin) {
        final Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(IncorrectAccountInformationException::new);
        if (!isPinCorrect(account, pin)) {
            throw new IncorrectAccountInformationException();
        } else {
            return account;
        }
    }

    private boolean isPinCorrect(Account account, String pin) {
        return account.getPin().equals(pin);
    }
}
