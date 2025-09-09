package com.leduc.staff.businessLayer.Employee;


import com.leduc.staff.businessLayer.Department.DepartmentService;
import com.leduc.staff.dataAccessLayer.Department.DepartmentIdentifier;
import com.leduc.staff.dataAccessLayer.Employee.Employee;
import com.leduc.staff.dataAccessLayer.Employee.EmployeeIdentifier;
import com.leduc.staff.dataAccessLayer.Employee.EmployeeRepository;
import com.leduc.staff.mappingLayer.Employee.EmployeeRequestMapper;
import com.leduc.staff.mappingLayer.Employee.EmployeeResponseMapper;
import com.leduc.staff.presentationLayer.Department.DepartmentResponseModel;
import com.leduc.staff.presentationLayer.Employee.EmployeeRequestModel;
import com.leduc.staff.presentationLayer.Employee.EmployeeResponseModel;
import com.leduc.staff.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;
    private final EmployeeRequestMapper employeeRequestMapper;
    private final EmployeeResponseMapper employeeResponseMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentService departmentService,
                               EmployeeRequestMapper employeeRequestMapper, EmployeeResponseMapper employeeResponseMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentService = departmentService;
        this.employeeRequestMapper = employeeRequestMapper;
        this.employeeResponseMapper = employeeResponseMapper;
    }

    @Override
    public List<EmployeeResponseModel> getEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            return new ArrayList<>();
        }

        List<EmployeeResponseModel> employeeResponseModelList = employeeResponseMapper.employeesToEmployeeResponseModels(employees);

        for (EmployeeResponseModel responseModel : employeeResponseModelList) {
            DepartmentResponseModel departmentResponse = departmentService.getDepartmentByDepartmentId(responseModel.getDepartmentId());
            if (departmentResponse != null) {
                responseModel.setDepartmentName(departmentResponse.getDepartmentName());
            }
        }

        return employeeResponseModelList;
    }

    @Override
    public EmployeeResponseModel getEmployeeById(String employeeId) {
        Employee foundEmployee = employeeRepository.findEmployeeByEmployeeIdentifier_EmployeeId(employeeId);
        if (foundEmployee == null) {
            throw new NotFoundException("Employee not found: " + employeeId);
        }
        if (employeeId.length() != 36){
            throw new IllegalArgumentException("Employee id length must be 36");
        }

        EmployeeResponseModel employeeResponseModel = employeeResponseMapper.employeeToEmployeeResponseModel(foundEmployee);

        DepartmentResponseModel departmentResponse = departmentService.getDepartmentByDepartmentId(foundEmployee.getDepartmentIdentifier().getDepartmentId());
        if (departmentResponse != null) {
            employeeResponseModel.setDepartmentName(departmentResponse.getDepartmentName());
        }

        return employeeResponseModel;
    }

    @Override
    public EmployeeResponseModel createEmployee(EmployeeRequestModel requestModel) {
        if (requestModel == null) {
            throw new IllegalArgumentException("Employee request model cannot be null");
        }

        if (requestModel.getDepartmentId() == null || requestModel.getDepartmentId().isEmpty()) {
            throw new IllegalArgumentException("Department ID is required");
        }

        EmployeeIdentifier employeeIdentifier = new EmployeeIdentifier();
        DepartmentIdentifier departmentIdentifier = new DepartmentIdentifier(requestModel.getDepartmentId());
        Employee employee = employeeRequestMapper.toEntity(requestModel, employeeIdentifier, departmentIdentifier);

        Employee savedEmployee = employeeRepository.save(employee);

        EmployeeResponseModel responseModel = employeeResponseMapper.employeeToEmployeeResponseModel(savedEmployee);

        DepartmentResponseModel departmentResponse = departmentService.getDepartmentByDepartmentId(savedEmployee.getDepartmentIdentifier().getDepartmentId());
        if (departmentResponse != null) {
            responseModel.setDepartmentName(departmentResponse.getDepartmentName());
        }

        return responseModel;
    }

    @Override
    public EmployeeResponseModel updateEmployee(String employeeId, EmployeeRequestModel requestModel) {
        Employee employee = employeeRepository.findEmployeeByEmployeeIdentifier_EmployeeId(employeeId);
        if (employee == null) {
            throw new NotFoundException("Employee not found for ID: " + employeeId);
        }
        if (employeeId.length() != 36){
            throw new IllegalArgumentException("Employee id length must be 36");
        }

        employee.setFirstName(requestModel.getFirstName());
        employee.setLastName(requestModel.getLastName());
        employee.setEmail(requestModel.getEmail());
        employee.setPositionTitle(requestModel.getPositionTitle());
        employee.setSalary(requestModel.getSalary());

        if (requestModel.getDepartmentId() != null) {
            DepartmentIdentifier departmentIdentifier = new DepartmentIdentifier(requestModel.getDepartmentId());
            employee.setDepartmentIdentifier(departmentIdentifier);
        }

        Employee savedUpdatedEmployee = employeeRepository.save(employee);

        EmployeeResponseModel responseModel = employeeResponseMapper.employeeToEmployeeResponseModel(savedUpdatedEmployee);

        DepartmentResponseModel departmentResponse = departmentService.getDepartmentByDepartmentId(savedUpdatedEmployee.getDepartmentIdentifier().getDepartmentId());
        if (departmentResponse != null) {
            responseModel.setDepartmentName(departmentResponse.getDepartmentName());
        }

        return responseModel;
    }

    @Override
    public void deleteEmployee(String employeeId) {
        Employee employee = employeeRepository.findEmployeeByEmployeeIdentifier_EmployeeId(employeeId);
        if (employee == null) {
            throw new NotFoundException("Employee not found for ID: " + employeeId);
        }
        if (employeeId.length() != 36){
            throw new IllegalArgumentException("Employee id length must be 36");
        }

        employeeRepository.delete(employee);
    }
}