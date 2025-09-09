package com.leduc.loans.presentationLayer;

import com.leduc.loans.dataAccessLayer.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseModel extends RepresentationModel<LoanResponseModel> {
    private String loanId;
    private String patronId;
    private String patronFirstName;
    private String patronLastName;
    private String employeeId;
    private String employeeFirstName;
    private String employeeLastName;
    private String bookId;
    private String isbn;
    private String title;
    private String authorFirstName;
    private String authorLastName;
    private String genre;
    private String bookType;
    private LocalDate issueDate;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
    private LoanStatus status;


}
