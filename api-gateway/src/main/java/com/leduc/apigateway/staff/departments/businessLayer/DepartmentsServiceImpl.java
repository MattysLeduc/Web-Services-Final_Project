package com.leduc.apigateway.staff.departments.businessLayer;

import com.leduc.apigateway.staff.departments.domainclientLayer.DepartmentsServiceClient;
import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentRequestModel;
import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentResponseModel;
import com.leduc.apigateway.staff.departments.presentationLayer.DepartmentsController;
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
public class DepartmentsServiceImpl implements DepartmentsService {

    private final DepartmentsServiceClient departmentsServiceClient;

    public DepartmentsServiceImpl(DepartmentsServiceClient departmentsServiceClient) {
        this.departmentsServiceClient = departmentsServiceClient;
    }

    @Override
    public List<DepartmentResponseModel> getDepartments() {
        log.debug("DepartmentsServiceImpl.getDepartments()");
        List<DepartmentResponseModel> depts = departmentsServiceClient.getAllDepartments();
        for (DepartmentResponseModel d : depts) {
            addHateoasLinks(d);
        }
        return depts;
    }

    @Override
    public DepartmentResponseModel getDepartmentById(String departmentId) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new InvalidInputException("Department ID must be exactly 36 characters long");
        }
        try {
            return addHateoasLinks(departmentsServiceClient.getDepartmentById(departmentId));
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Department with ID " + departmentId + " not found", ex);
        }
    }

    @Override
    public DepartmentResponseModel createDepartment(DepartmentRequestModel departmentRequest) {
        if (departmentRequest == null) {
            throw new InvalidInputException("DepartmentRequestModel must not be null");
        }
        return addHateoasLinks(departmentsServiceClient.createDepartment(departmentRequest));
    }

    @Override
    public DepartmentResponseModel updateDepartment(String departmentId, DepartmentRequestModel departmentRequest) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new InvalidInputException("Department ID must be exactly 36 characters long");
        }
        if (departmentRequest == null) {
            throw new InvalidInputException("DepartmentRequestModel must not be null");
        }
        try {
            return addHateoasLinks(departmentsServiceClient.updateDepartment(departmentId, departmentRequest));
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Department with ID " + departmentId + " not found", ex);
        }
    }

    @Override
    public void deleteDepartment(String departmentId) {
        if (departmentId == null || departmentId.length() != 36) {
            throw new InvalidInputException("Department ID must be exactly 36 characters long");
        }
        try {
            departmentsServiceClient.deleteDepartment(departmentId);
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Department with ID " + departmentId + " not found", ex);
        }
    }

    private DepartmentResponseModel addHateoasLinks(DepartmentResponseModel dept) {
        Link self = linkTo(methodOn(DepartmentsController.class)
                .getDepartmentById(dept.getDepartmentId())).withSelfRel();
        dept.add(self);

        Link all = linkTo(methodOn(DepartmentsController.class)
                .getDepartments()).withRel("all-departments");
        dept.add(all);

        return dept;
    }
}
