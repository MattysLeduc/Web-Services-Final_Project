package com.leduc.staff.dataAccessLayer.Employee;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
@AllArgsConstructor
public class EmployeeIdentifier {
    private String employeeId;

    public EmployeeIdentifier() {
        this.employeeId = UUID.randomUUID().toString();
    }
}