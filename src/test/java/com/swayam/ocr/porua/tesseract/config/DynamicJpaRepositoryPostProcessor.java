package com.swayam.ocr.porua.tesseract.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * This class is used to over-write a similar class in the implementation.
 */
public class DynamicJpaRepositoryPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
	System.err.println("********* Suppressing actual implementation of " + DynamicJpaRepositoryPostProcessor.class);
    }

}
