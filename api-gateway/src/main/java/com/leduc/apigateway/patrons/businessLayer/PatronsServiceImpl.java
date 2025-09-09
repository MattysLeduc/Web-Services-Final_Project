package com.leduc.apigateway.patrons.businessLayer;

import com.leduc.apigateway.patrons.domainclientLayer.PatronsServiceClient;
import com.leduc.apigateway.patrons.presentationLayer.PatronRequestModel;
import com.leduc.apigateway.patrons.presentationLayer.PatronResponseModel;
import com.leduc.apigateway.patrons.presentationLayer.PatronsController;
import com.leduc.apigateway.utils.exceptions.InvalidInputException;
import com.leduc.apigateway.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@Service
public class PatronsServiceImpl implements PatronsService {

    private final PatronsServiceClient patronsServiceClient;

    public PatronsServiceImpl(PatronsServiceClient patronsServiceClient) {
        this.patronsServiceClient = patronsServiceClient;
    }

    @Override
    public List<PatronResponseModel> getPatrons() {
        log.debug("PatronsServiceImpl.getPatrons()");
        List<PatronResponseModel> patrons = patronsServiceClient.getAllPatrons();
        for (PatronResponseModel p : patrons) {
            addHateoasLinks(p);
        }
        return patrons;
    }

    @Override
    public PatronResponseModel getPatronByPatronId(String patronId) {
        if (patronId == null || patronId.length() != 36) {
            throw new InvalidInputException("Patron ID must be exactly 36 characters long");
        }
        try {
            return addHateoasLinks(patronsServiceClient.getPatronByPatronId(patronId));
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Patron with ID " + patronId + " not found", ex);
        }
    }

    @Override
    public PatronResponseModel createPatron(PatronRequestModel requestModel) {
        if (requestModel == null) {
            throw new InvalidInputException("PatronRequestModel must not be null");
        }
        return addHateoasLinks(patronsServiceClient.createPatron(requestModel));
    }

    @Override
    public PatronResponseModel updatePatron(String patronId, PatronRequestModel requestModel) {
        if (patronId == null || patronId.length() != 36) {
            throw new InvalidInputException("Patron ID must be exactly 36 characters long");
        }
        if (requestModel == null) {
            throw new InvalidInputException("PatronRequestModel must not be null");
        }
        try {
            return addHateoasLinks(patronsServiceClient.updatePatron(patronId, requestModel));
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Patron with ID " + patronId + " not found", ex);
        }
    }

    @Override
    public void deletePatron(String patronId) {
        if (patronId == null || patronId.length() != 36) {
            throw new InvalidInputException("Patron ID must be exactly 36 characters long");
        }
        try {
            patronsServiceClient.deletePatron(patronId);
        } catch (EntityNotFoundException ex) {
            throw new NotFoundException("Patron with ID " + patronId + " not found", ex);
        }
    }

    private PatronResponseModel addHateoasLinks(PatronResponseModel patron) {
        Link self = linkTo(methodOn(PatronsController.class)
                .getPatronByPatronId(patron.getPatronId())).withSelfRel();
        patron.add(self);

        Link all = linkTo(methodOn(PatronsController.class)
                .getAllPatrons()).withRel("all-patrons");
        patron.add(all);

        return patron;
    }
}
