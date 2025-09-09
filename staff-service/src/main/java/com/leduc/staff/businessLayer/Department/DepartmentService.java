package com.leduc.staff.businessLayer.Department;




import com.leduc.staff.presentationLayer.Department.DepartmentRequestModel;
import com.leduc.staff.presentationLayer.Department.DepartmentResponseModel;

import java.util.List;


public interface DepartmentService {

    List<DepartmentResponseModel> getAllDepartments();
    DepartmentResponseModel getDepartmentByDepartmentId(String departmentId);
    DepartmentResponseModel createDepartment(DepartmentRequestModel requestDTO);
    DepartmentResponseModel updateDepartment(String departmentId, DepartmentRequestModel requestDTO);
    void deleteDepartment(String departmentId);
}
