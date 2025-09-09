package com.leduc.apigateway.loans.businessLayer;

import com.leduc.apigateway.loans.presentationLayer.LoanRequestModel;
import com.leduc.apigateway.loans.presentationLayer.LoanResponseModel;

import java.util.List;

public interface LoansService {
    List<LoanResponseModel> getAllLoans(String patronId);
    LoanResponseModel getLoanById(String patronId, String loanId);
    LoanResponseModel addLoan(LoanRequestModel loanRequestModel, String patronId);
    LoanResponseModel updateLoan(LoanRequestModel loanRequestModel, String patronId, String loanId);
    void removeLoan(String patronId, String loanId);
}
