package com.swayam.ocr.porua.tesseract.config;

import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class GoogleAuthenticationManager implements AuthenticationManager {

    @Override
    public Authentication authenticate(final Authentication authentication) {
	System.err.println("------------GoogleAuthenticationProvider.authenticate()");
	UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Hello", "", new ArrayList<>());
	return token;
    }

}
