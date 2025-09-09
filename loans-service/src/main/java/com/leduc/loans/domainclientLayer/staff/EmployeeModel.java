package com.leduc.loans.domainclientLayer.staff;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeModel {
    String employeeId;
    String firstName;
    String lastName;
}
