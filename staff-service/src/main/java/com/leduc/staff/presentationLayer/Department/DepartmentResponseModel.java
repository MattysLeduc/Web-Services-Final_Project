package com.leduc.staff.presentationLayer.Department;


import com.leduc.staff.dataAccessLayer.Department.DepartmentName;
import com.leduc.staff.dataAccessLayer.Department.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponseModel extends RepresentationModel<DepartmentResponseModel> {

    String departmentId;
    private DepartmentName departmentName;
    private Integer headCount;
    private List<Position> positions;

}
