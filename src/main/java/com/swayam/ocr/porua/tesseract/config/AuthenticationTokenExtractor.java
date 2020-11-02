package com.swayam.ocr.porua.tesseract.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

@Service
public class AuthenticationTokenExtractor implements AuthenticationConverter {

    // TODO FIXME: move this to config file
    private static final String CLIENT_ID = "955630342713-55eu6b3k5hmsg8grojjmk8mj1gi47g37.apps.googleusercontent.com";

    @Override
    public Authentication convert(HttpServletRequest request) {

	String idToken = request.getHeader("Authorization");

	if (!StringUtils.hasText(idToken)) {
	    throw new PreAuthenticatedCredentialsNotFoundException("Auth Token not found");
	}

	HttpTransport transport = new NetHttpTransport();
	JsonFactory jsonFactory = new JacksonFactory();
	GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
		// Specify the CLIENT_ID of the app that accesses the backend:
		.setAudience(Collections.singletonList(CLIENT_ID))
		// Or, if multiple clients access the backend:
		// .setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2,
		// CLIENT_ID_3))
		.build();

	GoogleIdToken googleToken;

	try {
	    googleToken = verifier.verify(idToken);
	} catch (GeneralSecurityException | IOException e) {
	    throw new AuthenticationServiceException("Error verifying auth token", e);
	}

	if (googleToken == null) {
	    throw new BadCredentialsException("Invalid token");
	}

	Payload payload = googleToken.getPayload();

	// Print user identifier
	String userId = payload.getSubject();
	System.out.println("User ID: " + userId);

	// Get profile information from payload
	String email = payload.getEmail();
	boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
	String name = (String) payload.get("name");
	String pictureUrl = (String) payload.get("picture");
	String locale = (String) payload.get("locale");
	String familyName = (String) payload.get("family_name");
	String givenName = (String) payload.get("given_name");

	return new UsernamePasswordAuthenticationToken(name, "", new ArrayList<>());
    }

}
