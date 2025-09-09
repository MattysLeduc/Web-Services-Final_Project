package com.leduc.loans.businessLayer;

import com.leduc.loans.presentationLayer.LoanRequestModel;
import com.leduc.loans.presentationLayer.LoanResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LoanService {
    List<LoanResponseModel> getAllLoans(String patronId);

    LoanResponseModel getLoanById(String patronId, String loanId);

    LoanResponseModel addLoan(LoanRequestModel loanRequestModel, String patronId);

    LoanResponseModel updateLoan(LoanRequestModel loanRequestModel, String patronId, String loanId);

    void deleteLoan(String patronId, String loanId);
}
