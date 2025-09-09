package com.leduc.apigateway.loans.businessLayer;

import com.leduc.apigateway.books.domainclientLayer.BooksServiceClient;
import com.leduc.apigateway.loans.domainclientLayer.LoanStatus;
import com.leduc.apigateway.loans.domainclientLayer.LoansServiceClient;
import com.leduc.apigateway.loans.presentationLayer.LoanRequestModel;
import com.leduc.apigateway.loans.presentationLayer.LoanResponseModel;
import com.leduc.apigateway.loans.presentationLayer.LoansController;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
public class LoansServiceImpl implements LoansService {

    private final LoansServiceClient loansServiceClient;
    private final BooksServiceClient booksServiceClient;


    public LoansServiceImpl(LoansServiceClient loansServiceClient, BooksServiceClient booksServiceClient) {
        this.loansServiceClient = loansServiceClient;
        this.booksServiceClient = booksServiceClient;
    }

    @Override
    public List<LoanResponseModel> getAllLoans(String patronId) {
        log.debug("LoansServiceImpl.getAllLoans({})", patronId);
        List<LoanResponseModel> loans = loansServiceClient.getAllLoans(patronId);
        for (LoanResponseModel loan : loans) {
            addHateoasLinks(loan);
        }
        return loans;
    }

    @Override
    public LoanResponseModel getLoanById(String patronId, String loanId) {
        log.debug("LoansServiceImpl.getLoanById({}, {})", patronId, loanId);

        if (loanId == null || loanId.length() != 36) {
            throw new InvalidInputException("Loan ID must be exactly 36 characters long");
        }

        try {
            LoanResponseModel loan = loansServiceClient.getLoanById(patronId, loanId);
            return addHateoasLinks(loan);
        } catch (NotFoundException ex) {
            throw new NotFoundException(
                    String.format("Loan with ID '%s' not found for patron '%s'", loanId, patronId),
                    ex
            );
        }
    }

    @Override
    public LoanResponseModel addLoan(LoanRequestModel loanRequestModel, String patronId) {
        log.debug("LoansServiceImpl.addLoan for patron {}", patronId);
        if (loanRequestModel == null) {
            throw new InvalidInputException("LoanRequestModel must not be null");
        }



        LoanResponseModel created = loansServiceClient.createLoan(patronId, loanRequestModel);
        return addHateoasLinks(created);
    }

    @Override
    public LoanResponseModel updateLoan(LoanRequestModel loanRequestModel,
                                        String patronId,
                                        String loanId) {
        log.debug("LoansServiceImpl.updateLoan({}, {})", patronId, loanId);
        if (loanId == null || loanId.length() != 36) {
            throw new InvalidInputException("Loan ID must be exactly 36 characters long");
        }
        if (loanRequestModel == null) {
            throw new InvalidInputException("LoanRequestModel must not be null");
        }

        try {
            LoanResponseModel updated = loansServiceClient.updateLoan(patronId, loanId, loanRequestModel);
            return addHateoasLinks(updated);
        } catch (NotFoundException ex) {
            throw new NotFoundException(
                    String.format("Loan with ID '%s' not found for patron '%s'", loanId, patronId),
                    ex
            );
        }
    }

    @Override
    public void removeLoan(String patronId, String loanId) {
        log.debug("LoansServiceImpl.removeLoan({}, {})", patronId, loanId);
        if (loanId == null || loanId.length() != 36) {
            throw new InvalidInputException("Loan ID must be exactly 36 characters long");
        }
        try {
            loansServiceClient.deleteLoan(patronId, loanId);
        } catch (NotFoundException ex) {
            throw new NotFoundException(
                    String.format("Loan with ID '%s' not found for patron '%s'", loanId, patronId),
                    ex
            );
        }
    }

    private LoanResponseModel addHateoasLinks(LoanResponseModel loan) {
        Link self = linkTo(methodOn(LoansController.class)
                .getLoanById(loan.getPatronId(), loan.getLoanId()))
                .withSelfRel();
        loan.add(self);

        Link all = linkTo(methodOn(LoansController.class)
                .getAllLoans(loan.getPatronId()))
                .withRel("all-loans");
        loan.add(all);

        return loan;
    }
}
