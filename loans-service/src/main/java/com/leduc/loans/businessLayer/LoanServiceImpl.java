package com.leduc.loans.businessLayer;


import com.leduc.loans.dataAccessLayer.Loan;
import com.leduc.loans.dataAccessLayer.LoanIdentifier;
import com.leduc.loans.dataAccessLayer.LoanRepository;
import com.leduc.loans.dataAccessLayer.LoanStatus;
import com.leduc.loans.domainclientLayer.books.BookModel;
import com.leduc.loans.domainclientLayer.books.BooksServiceClient;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.patrons.PatronsServiceClient;
import com.leduc.loans.domainclientLayer.staff.EmployeeModel;
import com.leduc.loans.domainclientLayer.staff.EmployeesServiceClient;
import com.leduc.loans.mappingLayer.LoanRequestMapper;
import com.leduc.loans.mappingLayer.LoanResponseMapper;
import com.leduc.loans.presentationLayer.LoanRequestModel;
import com.leduc.loans.presentationLayer.LoanResponseModel;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import com.leduc.loans.utils.exceptions.NotFoundException;
import com.leduc.loans.utils.exceptions.TooManyLoansException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    private final PatronsServiceClient patronsServiceClient;

    private final EmployeesServiceClient employeesServiceClient;

    private final BooksServiceClient booksServiceClient;

    private final LoanRequestMapper loanRequestMapper;

    private final LoanResponseMapper loanResponseMapper;

    public LoanServiceImpl(LoanRepository loanRepository, PatronsServiceClient patronsServiceClient, EmployeesServiceClient employeesServiceClient, BooksServiceClient booksServiceClient, LoanRequestMapper loanRequestMapper, LoanResponseMapper loanResponseMapper) {
        this.loanRepository = loanRepository;
        this.patronsServiceClient = patronsServiceClient;
        this.employeesServiceClient = employeesServiceClient;
        this.booksServiceClient = booksServiceClient;
        this.loanRequestMapper = loanRequestMapper;
        this.loanResponseMapper = loanResponseMapper;
    }

    @Override
    public List<LoanResponseModel> getAllLoans(String patronId) {
        PatronModel patron = patronsServiceClient.getPatronByPatronId(patronId);
        if (patron == null) {
            throw new InvalidInputException("Unknown patron id: " + patronId);
        }
        List<LoanResponseModel> results = new ArrayList<>();
        for (Loan loan : loanRepository.findAll()) {
            LoanResponseModel dto = loanResponseMapper.toResponse(loan);
            if (dto.getPatronId().equals(patronId)) {
                results.add(dto);
            }
        }
        return results;
    }

    @Override
    public LoanResponseModel getLoanById(String patronId, String loanId) {
        PatronModel patron = patronsServiceClient.getPatronByPatronId(patronId);
        if (patron == null) {
            throw new InvalidInputException("Unknown patron id: " + patronId);
        }

        if (loanRepository.getLoanByLoanIdentifier_LoanId(loanId) == null) {
            throw new NotFoundException("Loan id not found with id: " + loanId);
        }

        Loan loan = loanRepository.findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(patronId, loanId);

        LoanResponseModel dto = loanResponseMapper.toResponse(loan);
        if (!dto.getPatronId().equals(patronId)) {
            throw new InvalidInputException("Loan " + loanId + " does not belong to patron " + patronId);
        }
        return dto;
    }

    @Override
    public LoanResponseModel addLoan(LoanRequestModel loanRequestModel, String patronId) {
        // Existing create logic...
        PatronModel foundPatron = patronsServiceClient.getPatronByPatronId(patronId);
        if (foundPatron == null) {
            throw new NotFoundException("Patron not found for id: " + patronId);
        }
        Integer existingLoans = loanRepository.countByPatronModel_PatronId(patronId);
        if (existingLoans >= 3){
            throw new TooManyLoansException( String.format("Patron '%s' already has %d loans (max 3)", patronId, existingLoans));
        }
        PatronModel modelPatron = patronsServiceClient.getPatronByPatronId(loanRequestModel.getPatronId());
        if (modelPatron == null){
            throw new InvalidInputException("Unknown patron id: " + loanRequestModel.getPatronId());
        }
        BookModel foundBook = booksServiceClient.getBookByBookId(loanRequestModel.getBookId());
        if (foundBook == null) {
            throw new InvalidInputException("Unknown book id: " + loanRequestModel.getBookId());
        }
        EmployeeModel foundEmployee = employeesServiceClient.getEmployeeByEmployeeId(loanRequestModel.getEmployeeId());
        if (foundEmployee == null) {
            throw new InvalidInputException("Unknown employee id: " + loanRequestModel.getEmployeeId());
        }

        Loan loan = loanRequestMapper.requestModelToEntity(
                loanRequestModel,
                new LoanIdentifier(),
                foundBook,
                foundEmployee,
                foundPatron
        );

        booksServiceClient.patchBookCopiesAvailableByBookId(foundBook.getBookId(), loanRequestModel.getStatus());


        Loan savedLoan = loanRepository.save(loan);


        return loanResponseMapper.toResponse(savedLoan);
    }

    @Override
    public LoanResponseModel updateLoan(LoanRequestModel loanRequestModel,
                                        String patronId,
                                        String loanId) {

        // 1) Validate patron
        PatronModel patron = patronsServiceClient.getPatronByPatronId(patronId);
        if (patron == null) {
            throw new InvalidInputException("Unknown patron id: " + patronId);
        }

        // 2) Load existing Loan (brings in its Mongo _id)
        Loan existing = loanRepository
                .findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(patronId, loanId);
        if (existing == null) {
            throw new NotFoundException("Loan not found: " + loanId);
        }

        if (loanId.length() < 36){
            throw new InvalidInputException("Loan id: " + loanId + "must be 36 characters long");
        }

        // 3) Ensure it belongs to this patron
        LoanResponseModel existingDto = loanResponseMapper.toResponse(existing);
        if (!existingDto.getPatronId().equals(patronId)) {
            throw new InvalidInputException(
                    "Loan " + loanId + " does not belong to patron " + patronId);
        }

        // 4) Validate and fetch related Book and Employee
        BookModel foundBook = booksServiceClient.getBookByBookId(loanRequestModel.getBookId());
        if (foundBook == null) {
            throw new InvalidInputException(
                    "Unknown book id: " + loanRequestModel.getBookId());
        }
        EmployeeModel foundEmployee = employeesServiceClient
                .getEmployeeByEmployeeId(loanRequestModel.getEmployeeId());
        if (foundEmployee == null) {
            throw new InvalidInputException(
                    "Unknown employee id: " + loanRequestModel.getEmployeeId());
        }

        // 5) Mutate the existing entity’s fields
        existing.setBookModel(foundBook);
        existing.setEmployeeModel(foundEmployee);
        existing.setIssueDate(   loanRequestModel.getIssueDate()   );
        existing.setCheckoutDate(loanRequestModel.getCheckoutDate());
        existing.setReturnDate(  loanRequestModel.getReturnDate()  );
        existing.setStatus(      loanRequestModel.getStatus()      );

        booksServiceClient.patchBookCopiesAvailableByBookId(foundBook.getBookId(), loanRequestModel.getStatus());

        // 6) Save — this performs an update because `existing` carries its _id
        Loan saved = loanRepository.save(existing);

        // 7) Map to DTO and return
        return loanResponseMapper.toResponse(saved);
    }

    @Override
    public void deleteLoan(String patronId, String loanId) {
        // 1) Validate that the patron exists
        PatronModel patron = patronsServiceClient.getPatronByPatronId(patronId);
        if (patron == null) {
            throw new InvalidInputException("Unknown patron id: " + patronId);
        }

        // 2) Load the existing Loan (with its internal _id)
        Loan existing = loanRepository
                .findLoanByPatronModel_PatronIdAndLoanIdentifier_LoanId(patronId, loanId);
        if (existing == null) {
            throw new InvalidInputException("Loan not found: " + loanId);
        }

        if (loanRepository.getLoanByLoanIdentifier_LoanId(loanId) == null){
            throw new NotFoundException("Loan not found with id: " + loanId);
        }

        // 3) Delete the exact entity you loaded
        loanRepository.delete(existing);
    }
}
