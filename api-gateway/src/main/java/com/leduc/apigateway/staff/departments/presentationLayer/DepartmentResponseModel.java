package com.leduc.apigateway.staff.departments.presentationLayer;


import com.leduc.apigateway.staff.departments.domainclientLayer.DepartmentName;
import com.leduc.apigateway.staff.departments.domainclientLayer.Position;
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
