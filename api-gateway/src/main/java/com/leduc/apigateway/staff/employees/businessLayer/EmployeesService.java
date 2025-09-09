package com.leduc.apigateway.staff.employees.businessLayer;


import com.leduc.apigateway.staff.employees.presentationLayer.EmployeeRequestModel;
import com.leduc.apigateway.staff.employees.presentationLayer.EmployeeResponseModel;

import java.util.List;

public interface EmployeesService {

    List<EmployeeResponseModel> getAllEmployees();
    EmployeeResponseModel getEmployeeByEmployeeId(String employeeId);
    EmployeeResponseModel createEmployee(EmployeeRequestModel employeeRequestModel);
    EmployeeResponseModel updateEmployee(String employeeId,EmployeeRequestModel employeeRequestModel);
    void deleteEmployee(String employeeId);
}
