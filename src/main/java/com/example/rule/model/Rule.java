package com.example.rule.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author minnxu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Rule {

    @JsonProperty
    @JsonDeserialize(as = Long.class)
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String dslContent;

}
