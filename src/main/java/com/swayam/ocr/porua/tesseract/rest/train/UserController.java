package com.swayam.ocr.porua.tesseract.rest.train;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swayam.ocr.porua.tesseract.config.AuthenticationTokenExtractor;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.service.UserService;

@RestController
@RequestMapping("/ocr/train/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
	this.userService = userService;
    }

    @GetMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDetails newRegister(@RequestHeader(name = AuthenticationTokenExtractor.AUTH_TOKEN_NAME, required = true) String authenticationToken) {
	return userService.doNewRegistration(authenticationToken);
    }

}
