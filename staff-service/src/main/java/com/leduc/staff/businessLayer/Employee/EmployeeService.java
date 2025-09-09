package com.leduc.staff.businessLayer.Employee;




import com.leduc.staff.presentationLayer.Employee.EmployeeRequestModel;
import com.leduc.staff.presentationLayer.Employee.EmployeeResponseModel;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponseModel> getEmployees();
    EmployeeResponseModel getEmployeeById(String employeeId);
    EmployeeResponseModel createEmployee(EmployeeRequestModel requestModel);
    EmployeeResponseModel updateEmployee(String employeeId, EmployeeRequestModel requestModel);
    void deleteEmployee(String employeeId);
}
