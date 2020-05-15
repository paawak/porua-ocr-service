package com.swayam.ocr.porua.tesseract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.swayam.ocr.porua.tesseract.model.Language;
import com.swayam.ocr.porua.tesseract.model.RawOcrWord;

class TesseractOcrWordAnalyserIntegrationTest {

    @Test
    void testGetBoxStrings() throws URISyntaxException, IOException {
	// given
	List<String> expected = Files.readAllLines(Paths.get(TesseractOcrWordAnalyserIntegrationTest.class.getResource("/box-files/eng.Arial_Unicode_MS.exp0.png.box").toURI()));

	TesseractOcrWordAnalyser testClass =
		new TesseractOcrWordAnalyser(Paths.get(TesseractOcrWordAnalyserIntegrationTest.class.getResource("/box-files/eng.Arial_Unicode_MS.exp0.png").toURI()), Language.eng);

	Collection<RawOcrWord> rawOcrWords = testClass.getDetectedText();

	// when
	List<String> result = testClass.getBoxStrings(Collections.emptyMap(), rawOcrWords);

	// then
	assertEquals(expected, result);
    }

}
