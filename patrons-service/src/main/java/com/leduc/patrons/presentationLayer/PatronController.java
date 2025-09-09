package com.leduc.patrons.presentationLayer;

import com.leduc.patrons.businessLayer.PatronService;
import com.leduc.patrons.utils.exceptions.InvalidInputException;
import com.leduc.patrons.utils.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patrons")
public class PatronController {

    private final PatronService patronService;

    public PatronController(PatronService patronService) {
        this.patronService = patronService;
    }

    @GetMapping
    public List<PatronResponseModel> getAllPatrons() {
        return patronService.getPatrons();
    }

    @GetMapping("/{patronId}")
    public ResponseEntity<PatronResponseModel> getPatronById(@PathVariable String patronId) {
        if (patronId == null || patronId.length() != 36) {
            throw new NotFoundException("Patron ID must be exactly 36 characters long");
        }
        return ResponseEntity.ok(patronService.getPatronById(patronId));
    }

    @PostMapping
    public ResponseEntity<PatronResponseModel> createPatron(@RequestBody PatronRequestModel patronRequestModel) {
        if (patronRequestModel == null){
            throw new InvalidInputException("Patron Request cannot be null");
        }
        PatronResponseModel createdPatron = patronService.createPatron(patronRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPatron);
    }

    @PutMapping("/{patronId}")
    public ResponseEntity<PatronResponseModel> updatePatron(
            @PathVariable String patronId,
            @RequestBody PatronRequestModel patronRequestModel) {
        if (patronId == null || patronId.length() != 36) {
            throw new InvalidInputException("Patron ID must be exactly 36 characters long");
        }
        return ResponseEntity.ok(patronService.updatePatron(patronId, patronRequestModel));
    }

    @DeleteMapping("/{patronId}")
    public ResponseEntity<Void> deletePatron(@PathVariable String patronId) {
        if (patronId == null || patronId.length() != 36) {
            throw new InvalidInputException("Patron ID must be exactly 36 characters long");
        }
        patronService.deletePatron(patronId);
        return ResponseEntity.noContent().build();
    }
}
