package com.leduc.apigateway.staff.departments.businessLayer;

import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentRequestModel;
import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentResponseModel;

import java.util.List;

public interface DepartmentsService {
    List<DepartmentResponseModel> getDepartments();
    DepartmentResponseModel getDepartmentById(String departmentId);
    DepartmentResponseModel createDepartment(DepartmentRequestModel departmentRequest);
    DepartmentResponseModel updateDepartment(String departmentId, DepartmentRequestModel departmentRequest);
    void deleteDepartment(String departmentId);
}
