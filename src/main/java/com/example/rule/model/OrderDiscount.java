package com.example.rule.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author minnxu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class OrderDiscount {

    private double amount;

}
