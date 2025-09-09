package com.leduc.loans.dataAccessLayer;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LoanRepository extends MongoRepository<Loan, Integer> {
    List<Loan> findAllByPatronModel_PatronId(String patronId);

    Loan findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(String patronId, String loanId);

    Loan getLoanByLoanIdentifier_LoanId(String loanId);

    Integer countByPatronModel_PatronId(String patronId);
}
