package com.swayam.ocr.porua.tesseract.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.swayam.ocr.porua.tesseract.model.Book;
import com.swayam.ocr.porua.tesseract.repo.BookRepository;
import com.swayam.ocr.porua.tesseract.service.OcrWordServiceImpl.EntityClassDetails;

class OcrWordServiceImplTest {

    @Test
    void testEntityClassDetails() {
	// given
	BookRepository bookRepository = mock(BookRepository.class);
	Book book = new Book();
	book.setBaseTableName("my_abc_book_espanol");
	when(bookRepository.findById(23L)).thenReturn(Optional.of(book));

	OcrWordServiceImpl testClass = new OcrWordServiceImpl(bookRepository, null);

	// when
	EntityClassDetails result = testClass.getEntityClassDetails(23L);

	// then
	assertEquals("MyAbcBookEspanolOcrWordEntity", result.getOcrWordEntity());
	assertEquals("MyAbcBookEspanolCorrectedWordEntity", result.getCorrectedWordEntity());
	assertEquals("MyAbcBookEspanolOcrWordRepository", result.getOcrWordEntityRepository());
	assertEquals("MyAbcBookEspanolCorrectedWordRepository", result.getCorrectedWordEntityRepository());
    }

}
