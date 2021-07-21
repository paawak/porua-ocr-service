package com.swayam.ocr.porua.tesseract.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.swayam.ocr.porua.tesseract.config.ApplicationProperties;

@Service
public class FileSystemUtil {

    private final ApplicationProperties applicationProperties;

    public FileSystemUtil(ApplicationProperties applicationProperties) {
	this.applicationProperties = applicationProperties;

    }

    public Path getImageSaveLocation(String imageFileName) {
	return Paths.get(applicationProperties.getImageWriteDirectory()).resolve(imageFileName);
    }

    public Path saveMultipartFileAsImage(MultipartFile image) {
	Path imageOutputFilePath = getImageSaveLocation(image.getOriginalFilename());
	try {
	    Files.copy(image.getInputStream(), imageOutputFilePath);
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	return imageOutputFilePath;
    }

}
