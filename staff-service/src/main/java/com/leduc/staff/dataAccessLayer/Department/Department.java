package com.leduc.staff.dataAccessLayer.Department;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "departments")
@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private DepartmentIdentifier departmentIdentifier;


    @Enumerated(EnumType.STRING)
    private DepartmentName departmentName;

    private Integer headCount;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "department_positions", joinColumns = @JoinColumn(name = "department_id"))
    private List<Position> positions;

    public Department() {
        this.departmentIdentifier = new DepartmentIdentifier();
    }

    public Department(DepartmentName departmentName, Integer headCount, BigDecimal departmentBonus, List<Position> positions) {
        this.departmentName = departmentName;
        this.headCount = headCount;
        this.positions = positions;
    }
}
