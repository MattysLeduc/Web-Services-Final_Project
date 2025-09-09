package com.leduc.staff.presentationLayer.Employee;

import com.leduc.staff.businessLayer.Employee.EmployeeService;
import com.leduc.staff.presentationLayer.Employee.EmployeeRequestModel;
import com.leduc.staff.presentationLayer.Employee.EmployeeResponseModel;
import com.leduc.staff.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseModel>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getEmployees());
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseModel> getEmployeeById(@PathVariable String employeeId) {
        if (employeeId == null || employeeId.length() != 36) {
            throw new InvalidInputException("Employee ID must be exactly 36 characters long");
        }
        return ResponseEntity.ok(employeeService.getEmployeeById(employeeId));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseModel> createEmployee(@RequestBody EmployeeRequestModel requestModel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(requestModel));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseModel> updateEmployee(
            @PathVariable String employeeId,
            @RequestBody EmployeeRequestModel requestModel) {
        if (employeeId == null || employeeId.length() != 36) {
            throw new InvalidInputException("Employee ID must be exactly 36 characters long");
        }
        return ResponseEntity.ok(employeeService.updateEmployee(employeeId, requestModel));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String employeeId) {
        if (employeeId == null || employeeId.length() != 36) {
            throw new InvalidInputException("Employee ID must be exactly 36 characters long");
        }
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }
}
