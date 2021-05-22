package com.swayam.ocr.porua.tesseract.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class DynamicJpaRepositoryPostProcessorTest {

    @Test
    void testGetJarFilePath_1() throws MalformedURLException {
	// given
	DynamicJpaRepositoryPostProcessor testClass = new DynamicJpaRepositoryPostProcessor();

	// when
	Optional<URI> result = testClass.getJarFilePath(
		new URL("jar:file:/usr/lib/ocr/target/porua-ocr-service.jar!/BOOT-INF/classes!/"));

	// then
	assertEquals(URI.create("jar:file:/usr/lib/ocr/target/porua-ocr-service.jar"), result.get());
    }

    @Test
    void testGetJarFilePath_2() throws MalformedURLException {
	// given
	DynamicJpaRepositoryPostProcessor testClass = new DynamicJpaRepositoryPostProcessor();

	// when
	Optional<URI> result = testClass.getJarFilePath(new URL("file:/usr/lib/ocr/target/classes"));

	// then
	assertFalse(result.isPresent());
    }

    @Test
    void testGetJarFilePath_3() throws MalformedURLException {
	// given
	DynamicJpaRepositoryPostProcessor testClass = new DynamicJpaRepositoryPostProcessor();

	// when, then
	assertThrows("Unsupported protocol: https", IllegalArgumentException.class,
		() -> testClass.getJarFilePath(new URL("https://github.com/paawak/porua-ocr-service")));
    }

}
