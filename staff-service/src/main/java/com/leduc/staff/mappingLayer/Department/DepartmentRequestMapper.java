package com.leduc.staff.mappingLayer.Department;

import com.leduc.staff.dataAccessLayer.Department.Department;
import com.leduc.staff.presentationLayer.Department.DepartmentRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface DepartmentRequestMapper {

    @Mappings({
            @Mapping(source = "departmentRequestModel.departmentName", target = "departmentName"),
            @Mapping(source = "departmentRequestModel.headCount", target = "headCount"),
            @Mapping(source = "departmentRequestModel.positions", target = "positions")
    })
    Department departmentRequestModelToDepartment(DepartmentRequestModel departmentRequestModel);
}