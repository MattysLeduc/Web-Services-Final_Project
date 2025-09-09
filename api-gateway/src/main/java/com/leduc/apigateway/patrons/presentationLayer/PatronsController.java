package com.leduc.apigateway.patrons.presentationLayer;

import com.leduc.apigateway.patrons.businessLayer.PatronsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/patrons")
public class PatronsController {

    private final PatronsService patronsService;

    public PatronsController(PatronsService patronsService) {
        this.patronsService = patronsService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatronResponseModel>> getAllPatrons() {
        List<PatronResponseModel> patrons = patronsService.getPatrons();
        return ResponseEntity.ok(patrons);
    }

    @GetMapping(value = "/{patronId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatronResponseModel> getPatronByPatronId(@PathVariable String patronId) {
        log.debug("Request received in API-Gateway Patrons Controller: getPatronByPatronId");
        PatronResponseModel patron = patronsService.getPatronByPatronId(patronId);
        return ResponseEntity.ok(patron);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatronResponseModel> createPatron(@RequestBody PatronRequestModel patronRequest) {
        log.debug("Request received in API-Gateway Patrons Controller: createPatron");
        PatronResponseModel createdPatron = patronsService.createPatron(patronRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPatron);
    }

    @PutMapping(value = "/{patronId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatronResponseModel> updatePatron(@PathVariable String patronId,
                                                            @RequestBody PatronRequestModel patronRequest) {
        log.debug("Request received in API-Gateway Patrons Controller: updatePatron");
        PatronResponseModel updatedPatron = patronsService.updatePatron(patronId, patronRequest);
        return ResponseEntity.ok(updatedPatron);
    }

    @DeleteMapping(value = "/{patronId}")
    public ResponseEntity<Void> deletePatron(@PathVariable String patronId) {
        log.debug("Request received in API-Gateway Patrons Controller: deletePatron");
        patronsService.deletePatron(patronId);
        return ResponseEntity.noContent().build();
    }
}
