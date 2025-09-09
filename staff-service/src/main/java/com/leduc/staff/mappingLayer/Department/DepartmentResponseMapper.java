package com.leduc.staff.mappingLayer.Department;

import com.leduc.staff.dataAccessLayer.Department.Department;
import com.leduc.staff.presentationLayer.Department.DepartmentController;
import com.leduc.staff.presentationLayer.Department.DepartmentResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface DepartmentResponseMapper {

    @Mappings({
            @Mapping(source = "department.departmentIdentifier.departmentId", target = "departmentId"),
            @Mapping(source = "department.departmentName", target = "departmentName"),
            @Mapping(source = "department.headCount", target = "headCount"),
            @Mapping(source = "department.positions", target = "positions")
    })
    DepartmentResponseModel departmentResponseModel(Department department);

    List<DepartmentResponseModel> departmentResponseModelList(List<Department> departments);

}
