package com.zinkworks.atm.controller;

import com.zinkworks.atm.dto.BalanceDetails;
import com.zinkworks.atm.dto.OperationDetails;
import com.zinkworks.atm.service.ATMTasks;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/atm")
@AllArgsConstructor
public class ATMController {

    private ATMTasks atmTasks;

    @GetMapping("/check-balance")
    public ResponseEntity<BalanceDetails> getBalance(
            @RequestHeader("account-number") String accountNumber,
            @RequestHeader("pin") String pin) {
        return ResponseEntity.ok(atmTasks.getBalance(accountNumber, pin));
    }

    @PostMapping("/withdraw-money")
    public ResponseEntity<OperationDetails> getMoney(
            @RequestHeader("account-number") String accountNumber,
            @RequestHeader("pin") String pin,
            @RequestBody int amount) {
        return ResponseEntity.ok(atmTasks.withdrawnMoney(accountNumber, pin, amount));
    }
}
