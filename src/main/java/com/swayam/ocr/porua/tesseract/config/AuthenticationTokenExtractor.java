package com.swayam.ocr.porua.tesseract.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthenticationTokenExtractor implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {

	String idToken = request.getHeader("Authorization");

	if (!StringUtils.hasText(idToken)) {
	    throw new PreAuthenticatedCredentialsNotFoundException("Auth Token not found");
	}

	return new PreAuthenticatedAuthenticationToken("", idToken);
    }

}
