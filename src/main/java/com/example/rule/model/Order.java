package com.example.rule.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author minnxu
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Order {

    @JsonProperty
    private Customer customer;

    @JsonProperty
    private double amount;

}
