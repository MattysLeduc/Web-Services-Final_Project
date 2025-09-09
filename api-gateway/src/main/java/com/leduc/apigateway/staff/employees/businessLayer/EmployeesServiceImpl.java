package com.leduc.apigateway.staff.employees.businessLayer;

import com.leduc.apigateway.staff.employees.domainclientLayer.EmployeesServiceClient;
import com.leduc.apigateway.staff.employees.presentationLayer.EmployeeRequestModel;
import com.leduc.apigateway.staff.employees.presentationLayer.EmployeeResponseModel;
import com.leduc.apigateway.staff.employees.presentationLayer.EmployeesController;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@Service
public class EmployeesServiceImpl implements EmployeesService {

    private final EmployeesServiceClient employeesServiceClient;

    public EmployeesServiceImpl(EmployeesServiceClient employeesServiceClient) {
        this.employeesServiceClient = employeesServiceClient;
    }

    @Override
    public List<EmployeeResponseModel> getAllEmployees() {
        log.debug("EmployeesServiceImpl.getAllEmployees()");
        List<EmployeeResponseModel> emps = employeesServiceClient.getAllEmployees();
        for (EmployeeResponseModel e : emps) {
            addHateoasLinks(e);
        }
        return emps;
    }

    @Override
    public EmployeeResponseModel getEmployeeByEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.length() != 36) {
            throw new InvalidInputException("Employee ID must be exactly 36 characters long");
        }
        try {
            return addHateoasLinks(employeesServiceClient.getEmployeeByEmployeeId(employeeId));
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Employee with ID " + employeeId + " not found", ex);
        }
    }

    @Override
    public EmployeeResponseModel createEmployee(EmployeeRequestModel employeeRequestModel) {
        if (employeeRequestModel == null) {
            throw new InvalidInputException("EmployeeRequestModel must not be null");
        }
        return addHateoasLinks(employeesServiceClient.createEmployee(employeeRequestModel));
    }

    @Override
    public EmployeeResponseModel updateEmployee(String employeeId, EmployeeRequestModel employeeRequestModel) {
        if (employeeId == null || employeeId.length() != 36) {
            throw new InvalidInputException("Employee ID must be exactly 36 characters long");
        }
        if (employeeRequestModel == null) {
            throw new InvalidInputException("EmployeeRequestModel must not be null");
        }
        try {
            return addHateoasLinks(employeesServiceClient.updateEmployee(employeeId, employeeRequestModel));
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Employee with ID " + employeeId + " not found", ex);
        }
    }

    @Override
    public void deleteEmployee(String employeeId) {
        if (employeeId == null || employeeId.length() != 36) {
            throw new InvalidInputException("Employee ID must be exactly 36 characters long");
        }
        try {
            employeesServiceClient.deleteEmployee(employeeId);
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Employee with ID " + employeeId + " not found", ex);
        }
    }

    private EmployeeResponseModel addHateoasLinks(EmployeeResponseModel emp) {
        Link self = linkTo(methodOn(EmployeesController.class)
                .getEmployeeByEmployeeId(emp.getEmployeeId())).withSelfRel();
        emp.add(self);

        Link all = linkTo(methodOn(EmployeesController.class)
                .getAllEmployees()).withRel("all-employees");
        emp.add(all);

        return emp;
    }
}
