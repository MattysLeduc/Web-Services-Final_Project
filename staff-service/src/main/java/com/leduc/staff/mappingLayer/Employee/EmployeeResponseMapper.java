package com.leduc.staff.mappingLayer.Employee;

import com.leduc.staff.dataAccessLayer.Employee.Employee;
import com.leduc.staff.presentationLayer.Department.DepartmentController;
import com.leduc.staff.presentationLayer.Employee.EmployeeController;
import com.leduc.staff.presentationLayer.Employee.EmployeeResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface EmployeeResponseMapper {

    @Mappings({
            @Mapping(source = "employee.employeeIdentifier.employeeId", target = "employeeId"),
            @Mapping(source = "employee.firstName", target = "firstName"),
            @Mapping(source = "employee.lastName", target = "lastName"),
            @Mapping(source = "employee.email", target = "email"),
            @Mapping(source = "employee.phoneNumbers", target = "phoneNumbers"),
            @Mapping(source = "employee.employeeAddress.streetAddress", target = "streetAddress"),
            @Mapping(source = "employee.employeeAddress.city", target = "city"),
            @Mapping(source = "employee.employeeAddress.province", target = "province"),
            @Mapping(source = "employee.employeeAddress.country", target = "country"),
            @Mapping(source = "employee.employeeAddress.postalCode", target = "postalCode"),
            @Mapping(source = "employee.salary", target = "salary"),

            @Mapping(target = "departmentId", expression = "java(employee.getDepartmentIdentifier().getDepartmentId())"),

            @Mapping(source = "employee.positionTitle", target = "positionTitle")
    })
    EmployeeResponseModel employeeToEmployeeResponseModel(Employee employee);

    List<EmployeeResponseModel> employeesToEmployeeResponseModels(List<Employee> employees);

}

