package com.swayam.ocr.porua.tesseract.repo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.swayam.ocr.porua.tesseract.model.UserDetails;

public interface UserDetailsRepository extends CrudRepository<UserDetails, Long> {

    Optional<UserDetails> findByEmail(String email);

}
