package com.zinkworks.atm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Map;
import java.util.TreeMap;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ATM {

    @Id
    @GeneratedValue
    private Long id;

    @ElementCollection
    @MapKeyColumn(name = "denomination")
    @OrderBy("denomination DESC")
    private Map<Integer, Integer> denominationAmountMap = new TreeMap<>();

    public int getATMBalance() {
        int balance = 0;
        for (Map.Entry<Integer, Integer> entry : denominationAmountMap.entrySet()) {
            balance += entry.getKey() * entry.getValue();
        }
        return balance;
    }
}
