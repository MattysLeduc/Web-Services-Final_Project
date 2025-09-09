INSERT INTO departments(department_id, department_name, head_count)
VALUES ('1048b354-c18f-4109-8282-2a85485bfa5a', 'LIBRARY_SERVICES', 10);
INSERT INTO departments(department_id, department_name, head_count)
VALUES ('cb346554-8526-4569-849d-6abf41bb7f76', 'DIGITAL_RESOURCES', 5);


-- Department Positions Data
-- Library Services Department
INSERT INTO department_positions(department_id, title, code)
VALUES
    (1, 'LIBRARIAN', 'LIB'),
    (1, 'ASSISTANT_LIBRARIAN', 'ASST_LIB'),
    (1, 'CATALOGUER', 'CAT'),
    (1, 'LIBRARY_MANAGER', 'MAN');

-- Digital Resources Department
INSERT INTO department_positions(department_id, title, code)
VALUES
    (2, 'DIGITAL_RESOURCES_LIBRARIAN', 'DIG_LIB'),
    (2, 'REFERENCE_LIBRARIAN', 'REF_LIB'),
    (2, 'CHILDREN_LIBRARIAN', 'CHI_LIB');



-- Employee Data
-- Library Services Department Employees
INSERT INTO employees (employee_id, first_name, last_name, email_address, salary, street_address, city, province, country, postal_code, department_id, position_title)
VALUES ('e8a17e76-1c9f-4a6a-9342-488b7e99f0f7', 'Vilma', 'Chawner', 'vchawner0@phoca.cz', 34000.00, '8452 Anhalt Park', 'Chambly', 'Québec', 'Canada', 'J3L 5Y6', '1048b354-c18f-4109-8282-2a85485bfa5a', 'LIBRARIAN');

INSERT INTO employees (employee_id, first_name, last_name, email_address, salary, street_address, city, province, country, postal_code, department_id, position_title)
VALUES ('95e3de7a-c9bc-45cf-b6fc-b8f4c1157369', 'Winifred', 'Roy', 'wroy@phoca.cz', 75000.00, '9343 Main Street', 'Montreal', 'Québec', 'Canada', 'H1V 5Y6', '1048b354-c18f-4109-8282-2a85485bfa5a', 'ARCHIVIST');



-- Additional Employees from Various Departments
INSERT INTO employees (employee_id, first_name, last_name, email_address, salary, street_address, city, province, country, postal_code, department_id, position_title)
VALUES ('68f88e5f-3806-4b68-b227-945cd86d0de0', 'Sarah', 'Davis', 'sdavis@phoca.cz', 60000.00, '1457 King Street', 'Toronto', 'Ontario', 'Canada', 'M5A 1B2', '1048b354-c18f-4109-8282-2a85485bfa5a', 'CATALOGUER');

INSERT INTO employees (employee_id, first_name, last_name, email_address, salary, street_address, city, province, country, postal_code, department_id, position_title)
VALUES ('aad472a3-d8fe-4406-ae45-e98612262201', 'Michael', 'Taylor', 'mtaylor@phoca.cz', 77000.00, '1582 Queen Street', 'Ottawa', 'Ontario', 'Canada', 'K1S 3W4', '1048b354-c18f-4109-8282-2a85485bfa5a', 'ASSISTANT_LIBRARIAN');

insert into employee_phonenumbers(employee_id, type, number) values(1, 'WORK', '515-555-5555');
insert into employee_phonenumbers(employee_id, type, number) values(1, 'MOBILE', '514-555-4444');
insert into employee_phonenumbers(employee_id, type, number) values(2, 'WORK', '515-555-5587');
insert into employee_phonenumbers(employee_id, type, number) values(2, 'MOBILE', '514-555-1234');
insert into employee_phonenumbers(employee_id, type, number) values(3, 'WORK', '515-555-5554');
insert into employee_phonenumbers(employee_id, type, number) values(3, 'MOBILE', '514-555-3967');
insert into employee_phonenumbers(employee_id, type, number) values(3, 'HOME', '450-555-9876');
