package com.example.rule.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author minnxu
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Customer {

    @JsonProperty
    private String name;

    @JsonProperty
    private int age;

    @JsonProperty
    private CustomerType type;

    @JsonProperty
    private List<String> tags;

}
