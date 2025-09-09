package com.leduc.staff.dataAccessLayer.Employee;

import com.leduc.staff.dataAccessLayer.Department.DepartmentIdentifier;
import com.leduc.staff.dataAccessLayer.Department.PositionTitle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryIntegrationTests {


    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    public void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    public void whenSavingNewEmployee_thenItCanBeRetrievedByIdentifier() {
        // Arrange: Create a new employee instance
        Employee employee = new Employee();
        EmployeeIdentifier employeeIdentifier = new EmployeeIdentifier("12345678-1234-1234-1234-123456789012");
        employee.setEmployeeIdentifier(employeeIdentifier);
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setEmail("test.employee@example.com");
        employee.setSalary(new BigDecimal("75000"));
        EmployeeAddress employeeAddress = new EmployeeAddress("123 Test St","TestCity", "TestProvince", "TestCountry", "T1T 1T1");
        employee.setEmployeeAddress(employeeAddress);
        employee.setPositionTitle(PositionTitle.LIBRARIAN);
        DepartmentIdentifier deptIdentifier = new DepartmentIdentifier("00000000-0000-0000-0000-000000000000");
        employee.setDepartmentIdentifier(deptIdentifier);
        employee.setPhoneNumbers(Collections.emptyList());

        // Act: Save the employee
        Employee savedEmployee = employeeRepository.save(employee);

        // Assert: Ensure it was saved correctly
        assertNotNull(savedEmployee);
        assertNotNull(savedEmployee.getId());
        assertEquals("Test", savedEmployee.getFirstName());

        // Retrieve via repository custom finder
        Employee retrieved = employeeRepository.findEmployeeByEmployeeIdentifier_EmployeeId(employeeIdentifier.getEmployeeId());
        assertNotNull(retrieved);
        assertEquals("Test", retrieved.getFirstName());
    }

    @Test
    public void whenUpdatingExistingEmployee_thenChangesArePersisted() {
        // Arrange: Save an employee first
        Employee employee = new Employee();
        EmployeeIdentifier employeeIdentifier = new EmployeeIdentifier("22345678-1234-1234-1234-123456789012");
        employee.setEmployeeIdentifier(employeeIdentifier);
        employee.setFirstName("Update");
        employee.setLastName("Me");
        employee.setEmail("update.me@example.com");
        employee.setSalary(new BigDecimal("60000"));
        EmployeeAddress employeeAddress = new EmployeeAddress("456 Update St","Updatetown", "UpdateProvince", "USA", "U1U 1U1");
        employee.setEmployeeAddress(employeeAddress);
        employee.setPositionTitle(PositionTitle.ARCHIVIST);
        DepartmentIdentifier deptIdentifier = new DepartmentIdentifier("00000000-0000-0000-0000-000000000000");
        employee.setDepartmentIdentifier(deptIdentifier);
        employee.setPhoneNumbers(Collections.emptyList());

        Employee saved = employeeRepository.save(employee);
        assertNotNull(saved);

        // Act: Update fields and save again
        saved.setLastName("Updated");
        saved.setSalary(new BigDecimal("65000"));
        Employee updated = employeeRepository.save(saved);

        // Assert:
        assertNotNull(updated);
        assertEquals("Updated", updated.getLastName());
        assertEquals(new BigDecimal("65000"), updated.getSalary());
    }

    @Test
    public void whenDeletingExistingEmployee_thenItIsNoLongerFound() {
        // Arrange: Create and save an employee.
        Employee employee = new Employee();
        EmployeeIdentifier employeeIdentifier = new EmployeeIdentifier("32345678-1234-1234-1234-123456789012");
        employee.setEmployeeIdentifier(employeeIdentifier);
        employee.setFirstName("Delete");
        employee.setLastName("Me");
        employee.setEmail("delete.me@example.com");
        employee.setSalary(new BigDecimal("50000"));
        EmployeeAddress employeeAddress = new EmployeeAddress("789 Delete Rd", "Deletetown", "DelProvince", "USA", "D1D 1D1");
        employee.setEmployeeAddress(employeeAddress);
        employee.setPositionTitle(PositionTitle.CATALOGUER);
        DepartmentIdentifier deptIdentifier = new DepartmentIdentifier("00000000-0000-0000-0000-000000000000");
        employee.setDepartmentIdentifier(deptIdentifier);
        employee.setPhoneNumbers(Collections.emptyList());

        Employee saved = employeeRepository.save(employee);
        assertNotNull(saved);

        // Act: Delete the employee.
        employeeRepository.delete(saved);

        // Assert: It should no longer exist.
        Employee found = employeeRepository.findEmployeeByEmployeeIdentifier_EmployeeId(employeeIdentifier.getEmployeeId());
        assertNull(found);
    }

    @Test
    public void whenRetrievingAllEmployees_thenReturnAllSavedEmployees() {
        // Arrange: Create and save two employees.
        Employee employee1 = new Employee();
        EmployeeIdentifier identifier1 = new EmployeeIdentifier("42345678-1234-1234-1234-123456789012");
        employee1.setEmployeeIdentifier(identifier1);
        employee1.setFirstName("Alice");
        employee1.setLastName("Anderson");
        employee1.setEmail("alice.anderson@example.com");
        employee1.setSalary(new BigDecimal("70000"));
        EmployeeAddress address1 = new EmployeeAddress("101 A St","CityA", "ProvinceA", "USA", "A1A 1A1");
        employee1.setEmployeeAddress(address1);
        employee1.setPositionTitle(PositionTitle.CATALOGUER);
        DepartmentIdentifier deptIdentifier = new DepartmentIdentifier("00000000-0000-0000-0000-000000000000");
        employee1.setDepartmentIdentifier(deptIdentifier);
        employee1.setPhoneNumbers(Collections.emptyList());

        Employee employee2 = new Employee();
        EmployeeIdentifier identifier2 = new EmployeeIdentifier("52345678-1234-1234-1234-123456789012");
        employee2.setEmployeeIdentifier(identifier2);
        employee2.setFirstName("Bob");
        employee2.setLastName("Brown");
        employee2.setEmail("bob.brown@example.com");
        employee2.setSalary(new BigDecimal("80000"));
        EmployeeAddress address2 = new EmployeeAddress("202 B St","CityB", "ProvinceB", "USA", "B2B 2B2");
        employee2.setEmployeeAddress(address2);
        employee2.setPositionTitle(PositionTitle.LIBRARY_MANAGER);
        employee2.setDepartmentIdentifier(deptIdentifier);
        employee2.setPhoneNumbers(Collections.emptyList());

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        // Act: Retrieve all employees.
        List<Employee> allEmployees = employeeRepository.findAll();

        // Assert:
        assertNotNull(allEmployees);
        assertEquals(2, allEmployees.size());
    }

    @Test
    public void whenDeletingNullEmployee_thenThrowsInvalidDataAccessApiUsageException() {
        // Expect an exception when trying to delete null.
        assertThrows(InvalidDataAccessApiUsageException.class, () -> employeeRepository.delete(null));
    }

    @Test
    void whenConstructingEmployeeWithAllArgs_thenAllFieldsAreInitialized() {
        // Arrange: set up expected values for each constructor parameter
        EmployeeAddress expectedAddress = new EmployeeAddress(
                "123 Main St",
                "TestCity",
                "TestProvince",
                "TestCountry",
                "T1T 1T1"
        );
        List<EmployeePhoneNumber> expectedPhoneNumbers = new ArrayList<>();
        expectedPhoneNumbers.add(new EmployeePhoneNumber(PhoneType.MOBILE, "555-123-4567"));

        String expectedFirstName = "John";
        String expectedLastName = "Doe";
        String expectedEmail = "john.doe@example.com";
        BigDecimal expectedSalary = new BigDecimal("75000");

        DepartmentIdentifier expectedDeptId = new DepartmentIdentifier("00000000-0000-0000-0000-000000000000");
        PositionTitle expectedPosition = PositionTitle.LIBRARIAN;

        // Act: call the all-args constructor
        Employee employee = new Employee(
                expectedAddress,
                expectedPhoneNumbers,
                expectedFirstName,
                expectedLastName,
                expectedEmail,
                expectedSalary,
                expectedDeptId,
                expectedPosition
        );

        // Assert: verify that each field was assigned correctly
        assertNotNull(employee.getEmployeeIdentifier(), "Expected an auto-generated employee identifier object");
        assertEquals(expectedAddress, employee.getEmployeeAddress());
        assertEquals(expectedPhoneNumbers, employee.getPhoneNumbers());
        assertEquals(expectedFirstName, employee.getFirstName());
        assertEquals(expectedLastName, employee.getLastName());
        assertEquals(expectedEmail, employee.getEmail());
        assertEquals(expectedSalary, employee.getSalary());
        assertEquals(expectedDeptId, employee.getDepartmentIdentifier());
        assertEquals(expectedPosition, employee.getPositionTitle());
    }
}