package com.zinkworks.atm.dto;

import lombok.Value;

@Value
public class BalanceDetails {
    int balance;
    int overdraft;
    int total;

    public BalanceDetails(int balance, int overdraft) {
        this.balance = balance;
        this.overdraft = overdraft;
        this.total = balance + overdraft;
    }
}
