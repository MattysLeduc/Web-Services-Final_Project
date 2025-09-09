package com.leduc.staff.presentationLayer.Department;

import com.leduc.staff.businessLayer.Department.DepartmentService;
import com.leduc.staff.presentationLayer.Department.DepartmentRequestModel;
import com.leduc.staff.presentationLayer.Department.DepartmentResponseModel;
import com.leduc.staff.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseModel>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseModel> getDepartmentById(@PathVariable String departmentId) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new InvalidInputException("Department ID must be exactly 36 characters long");
        }
        return ResponseEntity.ok(departmentService.getDepartmentByDepartmentId(departmentId));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseModel> createDepartment(@RequestBody DepartmentRequestModel requestDTO) {
        if (requestDTO == null || requestDTO.getDepartmentName() == null) {
            throw new InvalidInputException("Department request cannot be null");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(requestDTO));
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseModel> updateDepartment(
            @PathVariable String departmentId,
            @RequestBody DepartmentRequestModel requestDTO) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new InvalidInputException("Department ID must be exactly 36 characters long");
        }
        if (requestDTO.getDepartmentName() == null) {
            throw new InvalidInputException("Department name cannot be null");
        }
        return ResponseEntity.ok(departmentService.updateDepartment(departmentId, requestDTO));
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String departmentId) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new InvalidInputException("Department ID must be exactly 36 characters long");
        }
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }
}
