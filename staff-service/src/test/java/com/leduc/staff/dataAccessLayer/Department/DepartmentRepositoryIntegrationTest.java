package com.leduc.staff.dataAccessLayer.Department;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class DepartmentRepositoryIntegrationTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setupDb() {
        departmentRepository.deleteAll();
    }

    @Test
    void whenDepartmentIsValid_thenSaveAndRetrieveDepartment() {
        // Arrange: Create a department with valid values.
        Department department = new Department();
        // Create a specific DepartmentIdentifier with a valid string.
        DepartmentIdentifier identifier = new DepartmentIdentifier("DEP-0001-UUID-STR");

        // Create a Position using the fixed enums.
        Position position = new Position(PositionTitle.ARCHIVIST, PositionCode.CAT);
        List<Position> positions = new ArrayList<>();
        positions.add(position);

        // Set up the department.
        department.setDepartmentIdentifier(identifier);
        department.setDepartmentName(DepartmentName.ARCHIVES_MANAGEMENT);
        department.setHeadCount(10);
        department.setPositions(positions);

        // Act: Save the department and then retrieve it.
        Department savedDepartment = departmentRepository.save(department);
        Department retrievedDepartment = departmentRepository.findByDepartmentIdentifier_DepartmentId(identifier.getDepartmentId());

        // Assert: Verify that the saved department was correctly persisted and retrieved.
        assertNotNull(savedDepartment);
        assertNotNull(retrievedDepartment);
        assertEquals(identifier.getDepartmentId(), retrievedDepartment.getDepartmentIdentifier().getDepartmentId());
        assertEquals(DepartmentName.ARCHIVES_MANAGEMENT, retrievedDepartment.getDepartmentName());
        assertEquals(10, retrievedDepartment.getHeadCount());
        assertNotNull(retrievedDepartment.getPositions());
        assertEquals(1, retrievedDepartment.getPositions().size());

        // Assert the position details.
        Position retrievedPosition = retrievedDepartment.getPositions().get(0);
        assertEquals(PositionTitle.ARCHIVIST, retrievedPosition.getTitle());
        assertEquals(PositionCode.CAT, retrievedPosition.getCode());
    }

    @Test
    void whenDepartmentDoesNotExist_thenReturnNull() {
        // Act: Query with a non-existent department identifier.
        String nonExistentId = "NON-EXISTENT-ID";
        Department found = departmentRepository.findByDepartmentIdentifier_DepartmentId(nonExistentId);
        // Assert
        assertNull(found);
    }

    @Test
    void whenMultipleDepartmentsAreSaved_thenFindAllReturnsCorrectCount() {
        // Arrange: Create two departments.
        Department department1 = new Department();
        DepartmentIdentifier id1 = new DepartmentIdentifier("DEP-0001-UUID-STR");
        Position position1 = new Position(PositionTitle.ARCHIVIST, PositionCode.CAT);
        List<Position> positions = new ArrayList<>();
        positions.add(position1);
        department1.setDepartmentIdentifier(id1);
        department1.setDepartmentName(DepartmentName.ARCHIVES_MANAGEMENT);
        department1.setHeadCount(10);
        department1.setPositions(positions);

        Department department2 = new Department();
        DepartmentIdentifier id2 = new DepartmentIdentifier("DEP-0002-UUID-STR");
        Position position2 = new Position(PositionTitle.ARCHIVIST, PositionCode.CAT);
        List<Position> positions2 = new ArrayList<>();
        positions2.add(position2);
        department2.setDepartmentIdentifier(id2);
        department2.setDepartmentName(DepartmentName.DIGITAL_RESOURCES);
        department2.setHeadCount(15);
        department2.setPositions(positions2);

        // Act
        departmentRepository.save(department1);
        departmentRepository.save(department2);
        List<Department> allDepartments = departmentRepository.findAll();

        // Assert
        assertNotNull(allDepartments);
        assertEquals(2, allDepartments.size());
        assertThat(allDepartments, hasItems(
                hasProperty("departmentIdentifier", hasProperty("departmentId", equalTo(id1.getDepartmentId()))),
                hasProperty("departmentIdentifier", hasProperty("departmentId", equalTo(id2.getDepartmentId())))
        ));
    }

    @Test
    void whenDepartmentIsUpdated_thenReturnUpdatedDepartment() {
        // Arrange: Create and save a department.
        Department department = new Department();
        DepartmentIdentifier identifier = new DepartmentIdentifier("DEP-0001-UUID-STR");
        Position position = new Position(PositionTitle.ARCHIVIST, PositionCode.CAT);
        List<Position> positions = new ArrayList<>();
        positions.add(position);
        department.setDepartmentIdentifier(identifier);
        department.setDepartmentName(DepartmentName.ARCHIVES_MANAGEMENT);
        department.setHeadCount(12);
        department.setPositions(positions);

        Department savedDepartment = departmentRepository.save(department);
        assertNotNull(savedDepartment);

        // Act: Update fields.
        savedDepartment.setDepartmentName(DepartmentName.ARCHIVES_MANAGEMENT);
        savedDepartment.setHeadCount(20);
        savedDepartment.setPositions(positions);
        Department updatedDepartment = departmentRepository.save(savedDepartment);

        // Assert
        assertNotNull(updatedDepartment);
        assertEquals(identifier.getDepartmentId(), updatedDepartment.getDepartmentIdentifier().getDepartmentId());
        assertEquals(DepartmentName.ARCHIVES_MANAGEMENT, updatedDepartment.getDepartmentName());
        assertEquals(20, updatedDepartment.getHeadCount());
        assertEquals(1, savedDepartment.getPositions().size());
    }

    @Test
    void whenDepartmentIsDeleted_thenReturnNull() {
        // Arrange: Create a department.
        Department department = new Department();
        DepartmentIdentifier identifier = new DepartmentIdentifier("DEP-0003-UUID-STR");
        Position position = new Position(PositionTitle.ARCHIVIST, PositionCode.CAT);
        List<Position> positions = new ArrayList<>();
        positions.add(position);
        department.setDepartmentIdentifier(identifier);
        department.setDepartmentName(DepartmentName.ARCHIVES_MANAGEMENT);
        department.setHeadCount(8);
        department.setPositions(positions);

        Department savedDepartment = departmentRepository.save(department);
        assertNotNull(savedDepartment);

        // Act: Delete the department.
        departmentRepository.delete(savedDepartment);
        Department foundDepartment = departmentRepository.findByDepartmentIdentifier_DepartmentId(identifier.getDepartmentId());
        // Assert
        assertNull(foundDepartment);
    }

    @Test
    void whenDepartmentIdIsNull_thenReturnNull() {
        // Act
        Department found = departmentRepository.findByDepartmentIdentifier_DepartmentId(null);
        // Assert
        assertNull(found);
    }

    @Test
    void whenDeletingNull_thenThrowException() {
        // Expect an exception when trying to delete a null reference.
        assertThrows(InvalidDataAccessApiUsageException.class, () -> departmentRepository.delete(null));
    }

}
