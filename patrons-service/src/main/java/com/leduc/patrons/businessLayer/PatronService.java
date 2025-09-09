package com.leduc.patrons.businessLayer;


import com.leduc.patrons.presentationLayer.PatronRequestModel;
import com.leduc.patrons.presentationLayer.PatronResponseModel;

import java.util.List;

public interface PatronService {

    List<PatronResponseModel> getPatrons();
    PatronResponseModel getPatronById(String patronId);
    PatronResponseModel createPatron(PatronRequestModel requestModel);
    PatronResponseModel updatePatron(String patronId, PatronRequestModel requestModel);
    void deletePatron(String patronId);
}
