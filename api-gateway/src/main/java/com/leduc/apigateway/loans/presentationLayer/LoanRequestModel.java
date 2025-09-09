package com.leduc.apigateway.loans.presentationLayer;

import com.leduc.apigateway.loans.domainclientLayer.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanRequestModel {
    private String patronId;
    private String bookId;
    private String employeeId;
    private LocalDate issueDate;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
    private LoanStatus status;

}
