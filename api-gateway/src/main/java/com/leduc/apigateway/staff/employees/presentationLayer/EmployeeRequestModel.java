package com.leduc.apigateway.staff.employees.presentationLayer;


import com.leduc.apigateway.staff.employees.domainclientLayer.EmployeePhoneNumber;
import com.leduc.apigateway.staff.employees.domainclientLayer.PositionTitle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequestModel {
        String firstName;
        String lastName;
        String email;
        List<EmployeePhoneNumber> phoneNumbers;

        String streetAddress;
        String city;
        String province;
        String country;
        String postalCode;

        BigDecimal salary;

        String departmentId;
        PositionTitle positionTitle;
}

