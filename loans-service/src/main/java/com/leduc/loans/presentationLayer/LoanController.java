package com.leduc.loans.presentationLayer;

import com.leduc.loans.businessLayer.LoanService;
import com.leduc.loans.domainclientLayer.patrons.PatronModel;
import com.leduc.loans.domainclientLayer.patrons.PatronsServiceClient;
import com.leduc.loans.utils.exceptions.InvalidInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patrons/{patronId}/loans")
public class LoanController {

    private final LoanService loanService;

    private final PatronsServiceClient patronsServiceClient;

    private static int UUID_LENGTH = 36;

    public LoanController(LoanService loanService, PatronsServiceClient patronsServiceClient) {
        this.loanService = loanService;
        this.patronsServiceClient = patronsServiceClient;
    }


    @GetMapping
    public ResponseEntity<List<LoanResponseModel>> getAllLoans(@PathVariable String patronId) {
        if (patronId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid patronId: " + patronId);
        }
        PatronModel patron = patronsServiceClient.getPatronByPatronId(patronId);
        if (patron == null) {
            return ResponseEntity.notFound().build();
        }
        List<LoanResponseModel> list = loanService.getAllLoans(patronId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponseModel> getLoanById(@PathVariable String patronId, @PathVariable String loanId) {
        if (patronId.length() != UUID_LENGTH || loanId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid ID");
        }
        LoanResponseModel dto = loanService.getLoanById(patronId, loanId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<LoanResponseModel> processPatronLoan(@RequestBody LoanRequestModel loanRequestModel, @PathVariable String patronId) {
        if (patronId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid patronId provided: " + patronId);
        }
        LoanResponseModel created = loanService.addLoan(loanRequestModel, patronId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{loanId}")
    public ResponseEntity<LoanResponseModel> updateLoan(@RequestBody LoanRequestModel loanRequestModel, @PathVariable String patronId, @PathVariable String loanId) {
        if (patronId.length() != UUID_LENGTH || loanId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid ID");
        }
        LoanResponseModel updated = loanService.updateLoan(loanRequestModel, patronId, loanId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<Void> deleteLoan(@PathVariable String patronId, @PathVariable String loanId) {
        if (patronId.length() != UUID_LENGTH || loanId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid ID");
        }
        loanService.deleteLoan(patronId, loanId);
        return ResponseEntity.noContent().build();
    }
}
