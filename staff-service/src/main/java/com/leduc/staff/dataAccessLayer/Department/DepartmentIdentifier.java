package com.leduc.staff.dataAccessLayer.Department;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
@AllArgsConstructor
public class DepartmentIdentifier {

    private String departmentId;

    public DepartmentIdentifier() {
        this.departmentId = UUID.randomUUID().toString();
    }
}
