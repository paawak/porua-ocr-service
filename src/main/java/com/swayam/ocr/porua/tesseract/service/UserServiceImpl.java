package com.swayam.ocr.porua.tesseract.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.UserRole;
import com.swayam.ocr.porua.tesseract.repo.UserDetailsRepository;

@Service
public class UserServiceImpl implements UserService {

    private List<String> ADMINS = Arrays.asList("paawak@gmail.com");

    private final UserDetailsRepository userDetailsRepository;

    public UserServiceImpl(UserDetailsRepository userDetailsRepository) {
	this.userDetailsRepository = userDetailsRepository;
    }

    @Override
    public UserDetails doNewRegistration(String authenticationToken) {
	Payload payload = new GoogleTokenVerifier().verifyToken(authenticationToken);
	String email = payload.getEmail();
	Optional<UserDetails> optionalUser = userDetailsRepository.findByEmail(email);
	if (optionalUser.isPresent()) {
	    return optionalUser.get();
	}

	UserDetails userDetails = new UserDetails();
	userDetails.setEmail(email);
	userDetails.setName((String) payload.get("name"));
	userDetails.setRole(ADMINS.contains(email) ? UserRole.ADMIN_ROLE : UserRole.CORRECTION_ROLE);

	return userDetailsRepository.save(userDetails);
    }

}
