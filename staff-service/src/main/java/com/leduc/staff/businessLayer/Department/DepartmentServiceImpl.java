package com.leduc.staff.businessLayer.Department;


import com.leduc.staff.dataAccessLayer.Department.Department;
import com.leduc.staff.dataAccessLayer.Department.DepartmentRepository;
import com.leduc.staff.mappingLayer.Department.DepartmentRequestMapper;
import com.leduc.staff.mappingLayer.Department.DepartmentResponseMapper;
import com.leduc.staff.presentationLayer.Department.DepartmentRequestModel;
import com.leduc.staff.presentationLayer.Department.DepartmentResponseModel;
import com.leduc.staff.utils.exceptions.DuplicateDepartmentException;
import com.leduc.staff.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentResponseMapper departmentResponseMapper;
    private final DepartmentRequestMapper departmentRequestMapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 DepartmentResponseMapper departmentResponseMapper,
                                 DepartmentRequestMapper departmentRequestMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentResponseMapper = departmentResponseMapper;
        this.departmentRequestMapper = departmentRequestMapper;
    }

    @Override
    public List<DepartmentResponseModel> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();


        return departmentResponseMapper.departmentResponseModelList(departments);
    }

    @Override
    public DepartmentResponseModel getDepartmentByDepartmentId(String departmentId) {
        Department department = departmentRepository.findByDepartmentIdentifier_DepartmentId(departmentId);

        if (department == null) {
            throw new NotFoundException("Department not found for ID: " + departmentId);
        }


        return departmentResponseMapper.departmentResponseModel(department);
    }

    @Override
    public DepartmentResponseModel createDepartment(DepartmentRequestModel requestDTO) {
        Department existingDepartment = departmentRepository.findByDepartmentName(requestDTO.getDepartmentName());
        if (existingDepartment != null) {
            throw new DuplicateDepartmentException("Department with name " + requestDTO.getDepartmentName() + " already exists");
        }

        Department department = departmentRequestMapper.departmentRequestModelToDepartment(requestDTO);

        department = departmentRepository.save(department);

        return departmentResponseMapper.departmentResponseModel(department);
    }

    @Override
    public DepartmentResponseModel updateDepartment(String departmentId, DepartmentRequestModel requestDTO) {
        Department department = departmentRepository.findByDepartmentIdentifier_DepartmentId(departmentId);
        if (department == null) {
            throw new NotFoundException("Department not found for ID: " + departmentId);
        }



        department.setDepartmentName(requestDTO.getDepartmentName());
        department.setHeadCount(requestDTO.getHeadCount());
        department.setPositions(requestDTO.getPositions());

        department = departmentRepository.save(department);
        return departmentResponseMapper.departmentResponseModel(department);
    }

    @Override
    public void deleteDepartment(String departmentId) {
        Department department = departmentRepository.findByDepartmentIdentifier_DepartmentId(departmentId);
        if (department == null) {
            throw new NotFoundException("Department not found for ID: " + departmentId);
        }


        departmentRepository.delete(department);
    }
}