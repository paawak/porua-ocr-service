package com.swayam.ocr.porua.tesseract.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Service;

@Service
public class GoogleAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final AuthenticationManager authenticationManager;

    public GoogleAuthenticationFilter(AuthenticationManager authenticationManager) {
	this.authenticationManager = authenticationManager;
	setAuthenticationManager(authenticationManager);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
	System.err.println("---------------GoogleAuthenticationFilter.getPreAuthenticatedPrincipal()");
	return "Palash Hardcoded";
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
	System.err.println("******************8");
	return "My Secret";
    }

}
