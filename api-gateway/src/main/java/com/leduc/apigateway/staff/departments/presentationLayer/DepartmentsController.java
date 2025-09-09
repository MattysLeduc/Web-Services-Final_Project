package com.leduc.apigateway.staff.departments.presentationLayer;

import com.leduc.apigateway.staff.departments.businessLayer.DepartmentsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/departments")
public class DepartmentsController {

    private final DepartmentsService departmentsService;

    public DepartmentsController(DepartmentsService departmentsService) {
        this.departmentsService = departmentsService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DepartmentResponseModel>> getDepartments() {
        List<DepartmentResponseModel> departments = departmentsService.getDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping(value = "/{departmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepartmentResponseModel> getDepartmentById(@PathVariable String departmentId) {
        DepartmentResponseModel department = departmentsService.getDepartmentById(departmentId);
        return ResponseEntity.ok(department);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepartmentResponseModel> createDepartment(@RequestBody DepartmentRequestModel departmentRequest) {
        DepartmentResponseModel createdDepartment = departmentsService.createDepartment(departmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    @PutMapping(value = "/{departmentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepartmentResponseModel> updateDepartment(@PathVariable String departmentId,
                                                                    @RequestBody DepartmentRequestModel departmentRequest) {
        DepartmentResponseModel updatedDepartment = departmentsService.updateDepartment(departmentId, departmentRequest);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping(value = "/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String departmentId) {
        departmentsService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }
}
