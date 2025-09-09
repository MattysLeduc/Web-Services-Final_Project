package com.leduc.loans.domainclientLayer.patrons;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatronModel {
    String patronId;
    String firstName;
    String lastName;
}
