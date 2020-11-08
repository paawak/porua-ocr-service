package com.swayam.ocr.porua.tesseract.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.UserRole;

public class GoogleAuthenticationManager implements AuthenticationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAuthenticationManager.class);

    // TODO FIXME: move this to config file
    private static final String CLIENT_ID = "955630342713-55eu6b3k5hmsg8grojjmk8mj1gi47g37.apps.googleusercontent.com";

    @Override
    public Authentication authenticate(final Authentication authentication) {
	LOGGER.info("start authentication...");
	HttpTransport transport = new NetHttpTransport();
	JsonFactory jsonFactory = new JacksonFactory();
	GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory).setAudience(Collections.singletonList(CLIENT_ID)).build();

	GoogleIdToken googleToken;

	try {
	    googleToken = verifier.verify((String) authentication.getCredentials());
	} catch (GeneralSecurityException | IOException e) {
	    LOGGER.warn("authentication failed while decoding token");
	    throw new AuthenticationServiceException("Error verifying auth token", e);
	}

	if (googleToken == null) {
	    LOGGER.warn("authentication failed: no token");
	    throw new BadCredentialsException("Invalid token");
	}

	Payload payload = googleToken.getPayload();

	String email = payload.getEmail();
	String name = (String) payload.get("name");

	// TODO FIXME get this from db
	UserRole role = UserRole.ADMIN_ROLE;

	LOGGER.trace("authentication success, userId: {}", payload.getSubject());

	UserDetails userDetails = new UserDetails(-1L, email, name, role);

	UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(name, "DontBotherBro", Arrays.asList(new SimpleGrantedAuthority(role.name())));
	token.setDetails(userDetails);
	return token;
    }

}
