package com.zinkworks.atm.service;

import com.zinkworks.atm.dto.BalanceDetails;
import com.zinkworks.atm.dto.OperationDetails;
import com.zinkworks.atm.entity.ATM;
import com.zinkworks.atm.entity.Account;
import com.zinkworks.atm.exceptions.ATMException;
import com.zinkworks.atm.exceptions.IncorrectAccountInformationException;
import com.zinkworks.atm.repository.ATMRepository;
import com.zinkworks.atm.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ATMTasksTest {

    private static ATM standardATM;
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ATMRepository atmRepository;

    @InjectMocks
    private ATMTasks atmTasks;

    @BeforeEach
    private void setupStandardATM() {
        final TreeMap<Integer, Integer> denominationBillsMap = new TreeMap<>(Comparator.reverseOrder());
        denominationBillsMap.put(50, 10);
        denominationBillsMap.put(20, 30);
        denominationBillsMap.put(10, 30);
        denominationBillsMap.put(5, 20);
        standardATM = new ATM(1L, denominationBillsMap);
    }

    @Test
    void shouldGetBalanceWhenPinAndAccountNumberAreCorrect() {
        final Account account = new Account(1L, "123456789", "1234", 1000, 10);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(account));

        final BalanceDetails balance = atmTasks.getBalance("123456789", "1234");

        Assertions.assertEquals(1000, balance.getBalance());
        assertAll(
                () -> assertEquals(1000, balance.getBalance()),
                () -> assertEquals(10, balance.getOverdraft()),
                () -> assertEquals(1010, balance.getTotal())
        );
    }

    @Test
    void shouldThrowExceptionWhenPinIsIncorrect() {
        final Account account = new Account(1L, "123456789", "1234", 1000, 0);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(account));

        assertThrows(IncorrectAccountInformationException.class,
                () -> atmTasks.getBalance("123456789", "1233"));
    }

    @Test
    void shouldThrowExceptionWhenThereIsNoAccountWithGivenAccountNumber() {
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());

        assertThrows(IncorrectAccountInformationException.class,
                () -> atmTasks.getBalance("123456780", "1234"));
    }

    @Test
    void shouldThrowExceptionWhenAmountToWithdrawIsGreaterThanATMBalance() {
        final Account account = new Account(1L, "123456789", "1234", 5000, 150);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(account));
        when(atmRepository.findById(1L)).thenReturn(Optional.of(standardATM));

        final ATMException atmException = assertThrows(ATMException.class,
                () -> atmTasks.withdrawnMoney("123456789", "1234", 4000));

        assertEquals("ATM does not have sufficient funds!", atmException.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenATMBalanceIsZero() {
        final Account account = new Account(1L, "123456789", "1234", 5000, 150);
        final TreeMap<Integer, Integer> denominationBillsMap = new TreeMap<>(Comparator.reverseOrder());
        denominationBillsMap.put(50, 0);
        denominationBillsMap.put(20, 0);
        denominationBillsMap.put(10, 0);
        denominationBillsMap.put(5, 0);
        ATM zeroFundsATM = new ATM(1L, denominationBillsMap);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(account));
        when(atmRepository.findById(1L)).thenReturn(Optional.of(zeroFundsATM));

        final ATMException atmException = assertThrows(ATMException.class,
                () -> atmTasks.withdrawnMoney("123456789", "1234", 4000));

        assertEquals("ATM run out of funds!", atmException.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAmountToWithdrawIsNotDividable() {
        final Account account = new Account(1L, "123456789", "1234", 5000, 150);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(account));
        when(atmRepository.findById(1L)).thenReturn(Optional.of(standardATM));

        final ATMException atmException = assertThrows(ATMException.class,
                () -> atmTasks.withdrawnMoney("123456789", "1234", 19));

        assertEquals("Could not withdraw money!", atmException.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAmountToWithdrawIsBelow5() {
        final Account account = new Account(1L, "123456789", "1234", 5000, 150);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(account));
        when(atmRepository.findById(1L)).thenReturn(Optional.of(standardATM));

        final ATMException atmException = assertThrows(ATMException.class,
                () -> atmTasks.withdrawnMoney("123456789", "1234", 3));

        assertEquals("Cannot withdraw amount below 5$", atmException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("ATMBalanceAfterOperation")
    void shouldProperlyChangeStatusOfATMWhenMoneyAreWithdrawn(int fiveBills, int tenBills, int twentyBills, int fiftyBills, int amount) {
        final Account account = new Account(1L, "123456789", "1234", 1230, 150);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(account));
        when(atmRepository.findById(1L)).thenReturn(Optional.of(standardATM));

        atmTasks.withdrawnMoney("123456789", "1234", amount);

        assertAll(
                () -> assertEquals(fiftyBills, standardATM.getDenominationAmountMap().get(50)),
                () -> assertEquals(twentyBills, standardATM.getDenominationAmountMap().get(20)),
                () -> assertEquals(tenBills, standardATM.getDenominationAmountMap().get(10)),
                () -> assertEquals(fiveBills, standardATM.getDenominationAmountMap().get(5))
        );
    }

    @ParameterizedTest
    @MethodSource("OperationDetails")
    void shouldProperlyCreateOperationDetailsWhenMoneyAreWithdrawn(Integer fiveBills, Integer tenBills, Integer twentyBills, Integer fiftyBills, Integer amount) {
        final Account account = new Account(1L, "123456789", "1234", 1230, 150);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(account));
        when(atmRepository.findById(1L)).thenReturn(Optional.of(standardATM));

        final OperationDetails operationDetails = atmTasks.withdrawnMoney("123456789", "1234", amount);

        assertAll(
                () -> assertEquals(amount, operationDetails.getAmountWithdrawn()),
                () -> assertEquals(fiftyBills, operationDetails.getNotesAmountMap().get(50)),
                () -> assertEquals(twentyBills, operationDetails.getNotesAmountMap().get(20)),
                () -> assertEquals(tenBills, operationDetails.getNotesAmountMap().get(10)),
                () -> assertEquals(fiveBills, operationDetails.getNotesAmountMap().get(5))
        );
    }

    private static Stream<Arguments> ATMBalanceAfterOperation() {
        return Stream.of(
                Arguments.of(20, 17, 0, 0, 1230),
                Arguments.of(20, 2, 0, 0, 1380),
                Arguments.of(19, 30, 30, 10, 5),
                Arguments.of(20, 29, 30, 10, 10),
                Arguments.of(20, 30, 29, 10, 20),
                Arguments.of(20, 30, 30, 9, 50),
                Arguments.of(19, 29, 30, 10, 15),
                Arguments.of(19, 30, 29, 10, 25),
                Arguments.of(19, 29, 29, 10, 35),
                Arguments.of(20, 30, 28, 10, 40),
                Arguments.of(19, 30, 28, 10, 45),
                Arguments.of(19, 30, 30, 9, 55),
                Arguments.of(20, 29, 30, 9, 60),
                Arguments.of(19, 29, 30, 9, 65),
                Arguments.of(20, 30, 29, 9, 70),
                Arguments.of(19, 30, 29, 9, 75),
                Arguments.of(20, 29, 29, 9, 80),
                Arguments.of(19, 29, 29, 9, 85),
                Arguments.of(20, 30, 28, 9, 90),
                Arguments.of(19, 30, 28, 9, 95),
                Arguments.of(20, 30, 30, 8, 100),
                Arguments.of(19, 30, 30, 8, 105)
        );
    }

    private static Stream<Arguments> OperationDetails() {
        return Stream.of(
                Arguments.of(null, 13, 30, 10, 1230),
                Arguments.of(null, 28, 30, 10, 1380),
                Arguments.of(1, null, null, null, 5),
                Arguments.of(null, 1, null, null, 10),
                Arguments.of(null, null, 1, null, 20),
                Arguments.of(null, null, null, 1, 50),
                Arguments.of(1, 1, null, null, 15),
                Arguments.of(1, null, 1, null, 25),
                Arguments.of(1, 1, 1, null, 35),
                Arguments.of(null, null, 2, null, 40),
                Arguments.of(1, null, 2, null, 45),
                Arguments.of(1, null, null, 1, 55),
                Arguments.of(null, 1, null, 1, 60),
                Arguments.of(1, 1, null, 1, 65),
                Arguments.of(null, null, 1, 1, 70),
                Arguments.of(1, null, 1, 1, 75),
                Arguments.of(null, 1, 1, 1, 80),
                Arguments.of(1, 1, 1, 1, 85),
                Arguments.of(null, null, 2, 1, 90),
                Arguments.of(1, null, 2, 1, 95),
                Arguments.of(null, null, null, 2, 100),
                Arguments.of(1, null, null, 2, 105)
        );
    }
}