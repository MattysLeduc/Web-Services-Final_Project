package com.leduc.staff.dataAccessLayer.Employee;


import com.leduc.staff.dataAccessLayer.Department.DepartmentIdentifier;
import com.leduc.staff.dataAccessLayer.Department.PositionTitle;
import jakarta.persistence.*;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private EmployeeIdentifier employeeIdentifier;

    private String firstName;

    private String lastName;

    @Column(name = "email_address", nullable = false)
    private String email;

    private BigDecimal salary;


    @ElementCollection
    @CollectionTable(name = "employee_phonenumbers", joinColumns = @JoinColumn(name = "employee_id"))
    private List<EmployeePhoneNumber> phoneNumbers;

    @Embedded
    private EmployeeAddress employeeAddress;

    @Embedded
    private DepartmentIdentifier departmentIdentifier;

    @Enumerated(EnumType.STRING)
    private PositionTitle positionTitle;




    public Employee() {
        this.employeeIdentifier = new EmployeeIdentifier();
    }

    public Employee(@NotNull EmployeeAddress employeeAddress, @NotNull List<EmployeePhoneNumber> phoneNumberList, @NotNull String firstName,
                    @NotNull String lastName, @NotNull String email, @NotNull BigDecimal salary,@NotNull DepartmentIdentifier departmentIdentifier, @NotNull PositionTitle positionTitle) {
        this.employeeIdentifier = new EmployeeIdentifier();

        this.employeeAddress = employeeAddress;
        this.phoneNumbers = phoneNumberList;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.salary = salary;
        this.departmentIdentifier = departmentIdentifier;
        this.positionTitle = positionTitle;
    }
}
