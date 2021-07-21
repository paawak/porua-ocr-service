package com.swayam.ocr.porua.tesseract.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app.config", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {

    private URI imageWriteDirectory;

    private String tessdataLocation;

    private String dynamicJpaWriteDirectory;

}
