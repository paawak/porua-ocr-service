package com.swayam.ocr.porua.tesseract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
@SpringBootApplication
public class TesseractOCRRestApplication {

    public static void main(String[] args) {
	SpringApplication.run(TesseractOCRRestApplication.class, args);
    }
}
