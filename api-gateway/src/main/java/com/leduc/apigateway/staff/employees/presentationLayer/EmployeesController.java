package com.leduc.apigateway.staff.employees.presentationLayer;





import com.leduc.apigateway.staff.employees.businessLayer.EmployeesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/staff")
public class EmployeesController {

    private final EmployeesService employeesService;

    public EmployeesController(EmployeesService employeesService) {
        this.employeesService = employeesService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmployeeResponseModel>> getAllEmployees() {
        List<EmployeeResponseModel> employees = employeesService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }


    @GetMapping(value = "/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponseModel> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        log.debug("1. Request Received in API-Gateway Employee Controller: getEmployeeByEmployeeId");
        return ResponseEntity.ok().body(employeesService.getEmployeeByEmployeeId(employeeId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponseModel> createEmployee(@RequestBody EmployeeRequestModel employeeRequest) {
        log.debug("Request Received in API-Gateway Employees Controller: createEmployee");
        EmployeeResponseModel createdEmployee = employeesService.createEmployee(employeeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @PutMapping(value = "/{employeeId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponseModel> updateEmployee(@PathVariable String employeeId,
                                                              @RequestBody EmployeeRequestModel employeeRequest) {
        log.debug("Request Received in API-Gateway Employees Controller: updateEmployee");
        EmployeeResponseModel updatedEmployee = employeesService.updateEmployee(employeeId, employeeRequest);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping(value = "/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String employeeId) {
        log.debug("Request Received in API-Gateway Employees Controller: deleteEmployee");
        employeesService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }
    
}
