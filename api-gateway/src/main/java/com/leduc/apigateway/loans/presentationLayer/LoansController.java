package com.leduc.apigateway.loans.presentationLayer;

import com.leduc.apigateway.loans.businessLayer.LoansService;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/patrons/{patronId}/loans")
public class LoansController {

    private final LoansService loansService;

    public LoansController(LoansService loansService) {
        this.loansService = loansService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LoanResponseModel>> getAllLoans(@PathVariable String patronId) {
        log.debug("Request received in LoansController: getAllLoans for patron {}", patronId);
        List<LoanResponseModel> loans = loansService.getAllLoans(patronId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping(value = "/{loanId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoanResponseModel> getLoanById(@PathVariable String patronId,
                                                         @PathVariable String loanId) {
        if (loansService.getLoanById(patronId, loanId) == null) {
            throw new EntityNotFoundException("Loan with id " + loanId + " not found for patron " + patronId);
        }
        log.debug("Request received in LoansController: getLoanById {} for patron {}", loanId, patronId);
        LoanResponseModel loan = loansService.getLoanById(patronId, loanId);
        return ResponseEntity.ok(loan);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoanResponseModel> createLoan(@PathVariable String patronId,
                                                        @RequestBody(required = false) LoanRequestModel loanRequest) {

        log.debug("Request received in LoansController: createLoan for patron {}", patronId);
        LoanResponseModel created = loansService.addLoan(loanRequest, patronId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/{loanId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoanResponseModel> updateLoan(@PathVariable String patronId,
                                                        @PathVariable String loanId,
                                                        @RequestBody(required = false) LoanRequestModel loanRequest) {
        if (loansService.getLoanById(patronId, loanId) == null) {
            throw new NotFoundException("Loan with id " + loanId + " not found for patron with id " + patronId);
        }
        log.debug("Request received in LoansController: updateLoan {} for patron {}", loanId, patronId);
        LoanResponseModel updated = loansService.updateLoan(loanRequest, patronId, loanId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/{loanId}")
    public ResponseEntity<Void> deleteLoan(@PathVariable String patronId,
                                           @PathVariable String loanId) {
        if (loansService.getLoanById(patronId,loanId) == null){
            throw new EntityNotFoundException("Loan with id: " + loanId + " not found for patron id: " + patronId);
        }
        log.debug("Request received in LoansController: deleteLoan {} for patron {}", loanId, patronId);
        loansService.removeLoan(patronId, loanId);
        return ResponseEntity.noContent().build();
    }
}
