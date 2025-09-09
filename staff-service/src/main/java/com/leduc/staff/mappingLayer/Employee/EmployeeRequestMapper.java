package com.leduc.staff.mappingLayer.Employee;

import com.leduc.staff.dataAccessLayer.Department.DepartmentIdentifier;
import com.leduc.staff.dataAccessLayer.Employee.Employee;
import com.leduc.staff.dataAccessLayer.Employee.EmployeeIdentifier;
import com.leduc.staff.presentationLayer.Employee.EmployeeRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface EmployeeRequestMapper {

    @Mappings({
            @Mapping(source = "employeeRequestModel.firstName", target = "firstName"),
            @Mapping(source = "employeeRequestModel.lastName", target = "lastName"),
            @Mapping(source = "employeeRequestModel.email", target = "email"),
            @Mapping(source = "employeeRequestModel.salary", target = "salary"),

            @Mapping(source = "employeeRequestModel.streetAddress", target = "employeeAddress.streetAddress"),
            @Mapping(source = "employeeRequestModel.city", target = "employeeAddress.city"),
            @Mapping(source = "employeeRequestModel.province", target = "employeeAddress.province"),
            @Mapping(source = "employeeRequestModel.country", target = "employeeAddress.country"),
            @Mapping(source = "employeeRequestModel.postalCode", target = "employeeAddress.postalCode"),

            @Mapping(source = "employeeRequestModel.phoneNumbers", target = "phoneNumbers"),

            @Mapping(expression = "java(departmentIdentifier)", target = "departmentIdentifier"),
            @Mapping(source = "employeeRequestModel.positionTitle", target = "positionTitle"),
            @Mapping(target = "id", ignore = true),
    })
    Employee toEntity(EmployeeRequestModel employeeRequestModel, EmployeeIdentifier employeeIdentifier, DepartmentIdentifier departmentIdentifier);
}
