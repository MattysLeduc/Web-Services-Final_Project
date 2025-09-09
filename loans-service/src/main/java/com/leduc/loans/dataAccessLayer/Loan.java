package com.leduc.loans.dataAccessLayer;

import com.leduc.loans.domainclientLayer.books.BookModel;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.staff.EmployeeModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "loans")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Loan {

    @Id
    private String id;

    private LoanIdentifier loanIdentifier;

    private PatronModel patronModel;

    private EmployeeModel employeeModel;

    private BookModel bookModel;

    private LocalDate issueDate;

    private LocalDate checkoutDate;

    private LocalDate returnDate;

    private LoanStatus status;
}
