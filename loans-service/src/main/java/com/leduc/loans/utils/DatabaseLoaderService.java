package com.leduc.loans.utils;

import com.leduc.loans.dataAccessLayer.Loan;
import com.leduc.loans.dataAccessLayer.LoanIdentifier;
import com.leduc.loans.dataAccessLayer.LoanRepository;

import com.leduc.loans.dataAccessLayer.LoanStatus;
import com.leduc.loans.domainclientLayer.books.BookModel;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.staff.EmployeeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseLoaderService implements CommandLineRunner {

    @Autowired
    LoanRepository loanRepository;

    @Override
    public void run(String... args) throws Exception {

        var loanIdentifier1 = new LoanIdentifier("2d8d1a47-08d8-4598-8b9d-6b2ec67dee1d");
        var bookModel = BookModel.builder()
                .bookId("6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a")
                .isbn("978-0-7432-7356-5")
                .title("A Brief History of Time")
                .authorFirstName("Stephen")
                .authorLastName("Hawking")
                .bookType("PAPERBACK")
                .genre("SCIENCE")
                .build();

        var patronModel = PatronModel.builder()
                .patronId("123e4567-e89b-12d3-a456-426614174000")
                .firstName("John")
                .lastName("Doe")
                .build();

        var employeeModel = EmployeeModel.builder()
                .employeeId("e8a17e76-1c9f-4a6a-9342-488b7e99f0f7")
                .firstName("Vilma")
                .lastName("Chawner")
                .build();

        var loan1 = Loan.builder()
                .loanIdentifier(loanIdentifier1)
                .bookModel(bookModel)
                .patronModel(patronModel)
                .employeeModel(employeeModel)
                .issueDate(LocalDate.of(2025, 4, 10))
                .returnDate(LocalDate.of(2025, 4, 17))
                .checkoutDate(LocalDate.of(2025, 4, 10))
                .status(LoanStatus.CHECKED_OUT)
                .build();

        loanRepository.save(loan1);
    }
}
