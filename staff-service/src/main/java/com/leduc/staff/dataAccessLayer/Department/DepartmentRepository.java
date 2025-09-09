package com.leduc.staff.dataAccessLayer.Department;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Department findByDepartmentIdentifier_DepartmentId(String departmentId);
    Department findByDepartmentName(DepartmentName departmentName);
}
