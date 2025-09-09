package com.leduc.staff.presentationLayer.Department;


import com.leduc.staff.dataAccessLayer.Department.DepartmentName;
import com.leduc.staff.dataAccessLayer.Department.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentRequestModel {
    private DepartmentName departmentName;
    private Integer headCount;
    private List<Position> positions;
}
