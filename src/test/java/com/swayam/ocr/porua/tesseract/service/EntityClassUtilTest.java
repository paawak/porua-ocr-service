package com.swayam.ocr.porua.tesseract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.swayam.ocr.porua.tesseract.service.EntityClassUtil.EntityClassDetails;

class EntityClassUtilTest {

    @Test
    void testEntityClassDetails() {
	// given
	EntityClassUtil testClass = new EntityClassUtil();

	// when
	EntityClassDetails result = testClass.getEntityClassDetails("my_abc_book_espanol");

	// then
	assertEquals("com.swayam.ocr.porua.tesseract.model.dynamic.MyAbcBookEspanolOcrWordEntity", result.getOcrWordEntity());
	assertEquals("com.swayam.ocr.porua.tesseract.model.dynamic.MyAbcBookEspanolCorrectedWordEntity", result.getCorrectedWordEntity());
	assertEquals("com.swayam.ocr.porua.tesseract.repo.dynamic.MyAbcBookEspanolOcrWordRepository", result.getOcrWordEntityRepository());
	assertEquals("com.swayam.ocr.porua.tesseract.repo.dynamic.MyAbcBookEspanolCorrectedWordRepository", result.getCorrectedWordEntityRepository());
    }

}
