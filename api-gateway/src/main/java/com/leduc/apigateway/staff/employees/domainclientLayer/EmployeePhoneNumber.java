package com.leduc.apigateway.staff.employees.domainclientLayer;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Getter
@NoArgsConstructor
public class EmployeePhoneNumber {

    @Enumerated(EnumType.STRING)
    private PhoneType type;
    private String number;

    public EmployeePhoneNumber(@NotNull PhoneType type, @NotNull String number) {
        this.type = type;
        this.number = number;
    }
}