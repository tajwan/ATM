package com.zinkworks.atm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class OperationDetails {
    private int amountWithdrawn;
    private Map<Integer, Integer> notesAmountMap;
}
