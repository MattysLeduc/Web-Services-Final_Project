package com.leduc.patrons.businessLayer;

import com.leduc.patrons.dataAccessLayer.*;
import com.leduc.patrons.mappingLayer.PatronRequestMapper;
import com.leduc.patrons.mappingLayer.PatronResponseMapper;
import com.leduc.patrons.presentationLayer.PatronRequestModel;
import com.leduc.patrons.presentationLayer.PatronResponseModel;
import com.leduc.patrons.utils.exceptions.DuplicatePatronException;
import com.leduc.patrons.utils.exceptions.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatronServiceImpl implements PatronService {

    private final PatronRepository patronRepository;
    private final PatronRequestMapper patronRequestMapper;
    private final PatronResponseMapper patronResponseMapper;

    public PatronServiceImpl(PatronRepository patronRepository,
                             PatronRequestMapper patronRequestMapper,
                             PatronResponseMapper patronResponseMapper) {
        this.patronRepository = patronRepository;
        this.patronRequestMapper = patronRequestMapper;
        this.patronResponseMapper = patronResponseMapper;
    }

    @Override
    public List<PatronResponseModel> getPatrons() {
        List<Patron> patrons = patronRepository.findAll();

        return patronResponseMapper.toResponseModelList(patrons);
    }

    @Override
    public PatronResponseModel getPatronById(String patronId) {
        Patron foundPatron = patronRepository.findPatronsByPatronIdentifier_PatronId(patronId);

        if (foundPatron == null) {
            throw new NotFoundException("Patron not found: " + patronId);
        }

        return patronResponseMapper.toResponseModel(foundPatron);
    }

    @Override
    public PatronResponseModel createPatron(PatronRequestModel requestModel) {
        // Validate request model

        if (requestModel.getEmail() != null && patronRepository.findByEmail(requestModel.getEmail()) != null) {
            throw new DuplicatePatronException("A patron with email " + requestModel.getEmail() + " already exists");
        }

        Patron patron = patronRequestMapper.toEntity(
                requestModel,
                new PatronIdentifier(),
                new PatronPhoneNumber()
        );

        patron = patronRepository.save(patron);

        return patronResponseMapper.toResponseModel(patron);
    }

    @Override
    public PatronResponseModel updatePatron(String patronId, PatronRequestModel requestModel) {
        // Retrieve the existing patron by ID
        Patron existingPatron = patronRepository.findPatronsByPatronIdentifier_PatronId(patronId);
        if (existingPatron == null) {
            throw new NotFoundException("Patron not found: " + patronId);
        }


            existingPatron.setFirstName(requestModel.getFirstName());

            existingPatron.setLastName(requestModel.getLastName());

            existingPatron.setEmail(requestModel.getEmail());

            existingPatron.setPassword(requestModel.getPassword());

            existingPatron.setMemberShipType(MemberShipType.valueOf(requestModel.getMemberShipType()));

        PatronAddress updatedAddress = new PatronAddress(
                requestModel.getStreetAddress(),
                requestModel.getCity(),
                requestModel.getProvince(),
                requestModel.getCountry(),
                requestModel.getPostalCode()
        );

        existingPatron.setPatronAddress(updatedAddress); // Set the new address

            existingPatron.setPhoneNumbers(requestModel.getPhoneNumbers());

        // Save the updated patron
        Patron updatedPatron = patronRepository.save(existingPatron);

        return patronResponseMapper.toResponseModel(updatedPatron);
    }




    @Override
    public void deletePatron(String patronId) {
        Patron foundPatron = patronRepository.findPatronsByPatronIdentifier_PatronId(patronId);
        if (foundPatron == null) {
            throw new NotFoundException("Patron not found: " + patronId);
        }
        patronRepository.delete(foundPatron);
    }
}
