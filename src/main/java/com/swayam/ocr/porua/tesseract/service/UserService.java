package com.swayam.ocr.porua.tesseract.service;

import com.swayam.ocr.porua.tesseract.model.UserDetails;

public interface UserService {

    UserDetails doNewRegistration(String authenticationToken);

}
