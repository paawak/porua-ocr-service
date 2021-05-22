package com.swayam.ocr.porua.tesseract.service;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.dynamic.DummyAuthorDummyBookCorrectedWordEntity;
import com.swayam.ocr.porua.tesseract.model.dynamic.DummyAuthorDummyBookOcrWordEntity;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordOutputDto;

@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@SpringBootTest
class OcrWordServiceImplIntegrationTest {

    private static final String CORRECTED_WORD_QUERY =
	    "SELECT id, user_id, ocr_word_id, corrected_text, ignored  FROM dummy_author_dummy_book_corrected_word";

    private static final String SELECT_FROM_OCR_WORD =
	    "SELECT book_id, page_image_id, word_sequence_id, raw_text, x1, y1, x2, y2, confidence FROM dummy_author_dummy_book_ocr_word ORDER BY word_sequence_id ASC";

    @Autowired
    private OcrWordServiceImpl testClass;

    @Autowired
    private JdbcOperations jdbcTemplate;

    @BeforeEach
    void setupBookAndRawImage() {
	jdbcTemplate.execute("DROP TABLE IF EXISTS dummy_author_dummy_book_ocr_word");
	jdbcTemplate.execute("DROP TABLE IF EXISTS dummy_author_dummy_book_corrected_word");
	jdbcTemplate.execute("TRUNCATE TABLE user");
	jdbcTemplate.execute("TRUNCATE TABLE page_image");
	jdbcTemplate.execute("TRUNCATE TABLE book");

	jdbcTemplate.execute("CREATE TABLE dummy_author_dummy_book_ocr_word (\n" + "  book_id int NOT NULL,\n"
		+ "  page_image_id int NOT NULL,\n" + "  word_sequence_id int NOT NULL,\n"
		+ "  raw_text varchar(255) NOT NULL,\n" + "  x1 int NOT NULL,\n" + "  y1 int NOT NULL,\n"
		+ "  x2 int NOT NULL,\n" + "  y2 int NOT NULL,\n" + "  confidence double NOT NULL,\n"
		+ "  line_number int,\n" + "  id INTEGER IDENTITY PRIMARY KEY\n" + ")");

	jdbcTemplate.execute("CREATE TABLE dummy_author_dummy_book_corrected_word (\n"
		+ "  id INTEGER IDENTITY PRIMARY KEY,\n" + "  user_id int NOT NULL,\n"
		+ "  ocr_word_id int NOT NULL,\n" + "  corrected_text varchar(255),\n"
		+ "  ignored boolean NOT NULL \n" + ") ");

	jdbcTemplate.update(
		"INSERT INTO book (id, name, language, base_table_name) VALUES (1, 'TEST BOOK 1', 'ben', 'dummy_author_dummy_book')");
	jdbcTemplate.update(
		"INSERT INTO book (id, name, language, base_table_name) VALUES (2, 'TEST BOOK 2', 'eng', 'dummy_author_dummy_book')");
	jdbcTemplate.update(
		"INSERT INTO page_image (id, book_id, name, page_number) VALUES (1, 1, 'TEST IMAGE 1.jpg', 1)");
	jdbcTemplate.update(
		"INSERT INTO page_image (id, book_id, name, page_number) VALUES (2, 1, 'TEST IMAGE 2.jpg', 2)");

	jdbcTemplate.execute(
		"INSERT INTO user (id, name, email, role) VALUES (1, 'Test User', 'testing@gmail.com', 'CORRECTION_ROLE')");
    }

    @Test
    void testAddOcrWord() {
	// given
	DummyAuthorDummyBookOcrWordEntity ocrWord = getOcrWord(1, 1, 11, 22, 33, 44, 55.55f, "ABC123", 1);

	List<DummyAuthorDummyBookOcrWordEntity> expected = Arrays.asList(ocrWord);

	// when
	testClass.addOcrWord(ocrWord);

	// then
	List<DummyAuthorDummyBookOcrWordEntity> results =
		jdbcTemplate.query(SELECT_FROM_OCR_WORD, ocrWordMapper());

	assertEquals(expected, results);
    }

    @Test
    void testUpdateCorrectTextInOcrWord() {
	// given
	DummyAuthorDummyBookOcrWordEntity ocrWord1 = getOcrWord(1, 1, 11, 22, 33, 44, 55.55f, "ABC123", 1);
	DummyAuthorDummyBookOcrWordEntity ocrWord2 =
		getOcrWord(1, 1, 111, 222, 333, 444, 555.555f, "DEF456", 2);
	DummyAuthorDummyBookOcrWordEntity ocrWord3 =
		getOcrWord(1, 1, 1111, 2222, 3333, 4444, 5555.5555f, "GHI789", 3);

	DummyAuthorDummyBookOcrWordEntity ocrWord2_1 =
		getOcrWord(1, 1, 111, 222, 333, 444, 555.555f, "DEF456", 2);

	List<DummyAuthorDummyBookOcrWordEntity> expected = Arrays.asList(ocrWord1, ocrWord2_1, ocrWord3);

	testClass.addOcrWord(ocrWord1);
	testClass.addOcrWord(ocrWord2);
	testClass.addOcrWord(ocrWord3);

	UserDetails user = new UserDetails();
	user.setId(1);

	// when
	testClass.updateCorrectTextInOcrWord(new OcrWordId(1, 1, 2), "I have changed", user);

	// then
	List<DummyAuthorDummyBookOcrWordEntity> ocrWords =
		jdbcTemplate.query(SELECT_FROM_OCR_WORD, ocrWordMapper());

	assertEquals(expected, ocrWords);

	List<DummyAuthorDummyBookCorrectedWordEntity> correctedWords =
		jdbcTemplate.query(CORRECTED_WORD_QUERY, correctedWordMapper());

	assertEquals(1, correctedWords.size());
	DummyAuthorDummyBookCorrectedWordEntity correctedWord = correctedWords.get(0);
	assertEquals("I have changed", correctedWord.getCorrectedText());
	assertEquals(0, correctedWord.getId());
	assertFalse(correctedWord.isIgnored());
	assertEquals(1, correctedWord.getOcrWordId());
	assertEquals(1, correctedWord.getUser().getId());
    }

    @Test
    void testMarkWordAsIgnored() {
	// given
	DummyAuthorDummyBookOcrWordEntity ocrWord1 = getOcrWord(1, 1, 11, 22, 33, 44, 55.55f, "ABC123", 1);
	DummyAuthorDummyBookOcrWordEntity ocrWord2 =
		getOcrWord(1, 1, 111, 222, 333, 444, 555.555f, "DEF456", 2);
	DummyAuthorDummyBookOcrWordEntity ocrWord3 =
		getOcrWord(1, 1, 1111, 2222, 3333, 4444, 5555.5555f, "GHI789", 3);

	testClass.addOcrWord(ocrWord1);
	testClass.addOcrWord(ocrWord2);
	testClass.addOcrWord(ocrWord3);

	UserDetails user = new UserDetails();
	user.setId(1);

	// when
	int result = testClass.markWordAsIgnored(new OcrWordId(1, 1, 2), user);

	// then
	assertEquals(1, result);
	List<DummyAuthorDummyBookOcrWordEntity> words =
		jdbcTemplate.query(SELECT_FROM_OCR_WORD, ocrWordMapper());
	assertEquals(Arrays.asList(ocrWord1, ocrWord2, ocrWord3), words);

	List<DummyAuthorDummyBookCorrectedWordEntity> correctedWords =
		jdbcTemplate.query(CORRECTED_WORD_QUERY, correctedWordMapper());

	assertEquals(1, correctedWords.size());
	DummyAuthorDummyBookCorrectedWordEntity correctedWord = correctedWords.get(0);
	assertNull(correctedWord.getCorrectedText());
	assertEquals(0, correctedWord.getId());
	assertTrue(correctedWord.isIgnored());
	assertEquals(1, correctedWord.getOcrWordId());
	assertEquals(1, correctedWord.getUser().getId());
    }

    @Test
    void testGetWords() {
	// given
	DummyAuthorDummyBookOcrWordEntity ocrWord1 = getOcrWord(1, 1, 11, 22, 33, 44, 55.55f, "ABC123", 1);
	DummyAuthorDummyBookOcrWordEntity ocrWord2 =
		getOcrWord(1, 1, 111, 222, 333, 444, 555.555f, "DEF456", 2);
	DummyAuthorDummyBookOcrWordEntity ocrWord3 =
		getOcrWord(1, 1, 1111, 2222, 3333, 4444, 5555.5555f, "GHI789", 3);
	DummyAuthorDummyBookOcrWordEntity ocrWord4 = getOcrWord(1, 2, 11, 22, 33, 44, 55.55f, "ABC123", 1);
	DummyAuthorDummyBookOcrWordEntity ocrWord5 =
		getOcrWord(1, 2, 111, 222, 333, 444, 555.555f, "DEF456", 2);
	DummyAuthorDummyBookOcrWordEntity ocrWord6 =
		getOcrWord(1, 2, 1111, 2222, 3333, 4444, 5555.5555f, "GHI789", 3);
	DummyAuthorDummyBookOcrWordEntity ocrWord7 = getOcrWord(2, 2, 11, 22, 33, 44, 55.55f, "ABC123", 1);
	DummyAuthorDummyBookOcrWordEntity ocrWord8 =
		getOcrWord(2, 2, 111, 222, 333, 444, 555.555f, "DEF456", 2);
	DummyAuthorDummyBookOcrWordEntity ocrWord9 =
		getOcrWord(2, 2, 1111, 2222, 3333, 4444, 5555.5555f, "GHI789", 3);

	List<DummyAuthorDummyBookOcrWordEntity> toBeInserted = Arrays.asList(ocrWord1, ocrWord2, ocrWord3,
		ocrWord4, ocrWord5, ocrWord6, ocrWord7, ocrWord8, ocrWord9);

	OcrWordOutputDto outputDto1 = new OcrWordOutputDto();
	BeanUtils.copyProperties(ocrWord1, outputDto1);
	outputDto1.setId(0);
	OcrWordOutputDto outputDto2 = new OcrWordOutputDto();
	BeanUtils.copyProperties(ocrWord2, outputDto2);
	outputDto2.setId(1);
	OcrWordOutputDto outputDto3 = new OcrWordOutputDto();
	BeanUtils.copyProperties(ocrWord3, outputDto3);
	outputDto3.setId(2);

	List<OcrWordOutputDto> expected = Arrays.asList(outputDto1, outputDto2, outputDto3);

	toBeInserted.forEach(ocrWord -> testClass.addOcrWord(ocrWord));

	// when
	Collection<OcrWordOutputDto> results = testClass.getWords(1, 1, new UserDetails());

	// then
	assertEquals(3, results.size());
	assertEquals(expected, results);
    }

    @Test
    void testGetWordCount() {
	// given
	DummyAuthorDummyBookOcrWordEntity ocrWord1 = getOcrWord(1, 1, 11, 22, 33, 44, 55.55f, "ABC123", 1);
	DummyAuthorDummyBookOcrWordEntity ocrWord2 =
		getOcrWord(1, 1, 111, 222, 333, 444, 555.555f, "DEF456", 2);
	DummyAuthorDummyBookOcrWordEntity ocrWord3 =
		getOcrWord(1, 1, 1111, 2222, 3333, 4444, 5555.5555f, "GHI789", 3);
	DummyAuthorDummyBookOcrWordEntity ocrWord4 = getOcrWord(1, 2, 11, 22, 33, 44, 55.55f, "ABC123", 1);
	DummyAuthorDummyBookOcrWordEntity ocrWord5 =
		getOcrWord(1, 2, 111, 222, 333, 444, 555.555f, "DEF456", 2);
	DummyAuthorDummyBookOcrWordEntity ocrWord6 =
		getOcrWord(1, 2, 1111, 2222, 3333, 4444, 5555.5555f, "GHI789", 3);
	DummyAuthorDummyBookOcrWordEntity ocrWord7 = getOcrWord(2, 2, 11, 22, 33, 44, 55.55f, "ABC123", 1);
	DummyAuthorDummyBookOcrWordEntity ocrWord8 =
		getOcrWord(2, 2, 111, 222, 333, 444, 555.555f, "DEF456", 2);
	DummyAuthorDummyBookOcrWordEntity ocrWord9 =
		getOcrWord(2, 2, 1111, 2222, 3333, 4444, 5555.5555f, "GHI789", 3);

	List<DummyAuthorDummyBookOcrWordEntity> toBeInserted = Arrays.asList(ocrWord1, ocrWord2, ocrWord3,
		ocrWord4, ocrWord5, ocrWord6, ocrWord7, ocrWord8, ocrWord9);

	toBeInserted.forEach(ocrWord -> testClass.addOcrWord(ocrWord));

	// when
	int result = testClass.getWordCount(1, 1);

	// then
	assertEquals(3, result);
    }

    private RowMapper<DummyAuthorDummyBookOcrWordEntity> ocrWordMapper() {
	return (ResultSet res, int rowNum) -> {
	    return getOcrWord(1, 1, res.getInt("x1"), res.getInt("y1"), res.getInt("x2"), res.getInt("y2"),
		    res.getFloat("confidence"), res.getString("raw_text"), res.getInt("word_sequence_id"));
	};
    }

    private RowMapper<DummyAuthorDummyBookCorrectedWordEntity> correctedWordMapper() {
	return (ResultSet res, int rowNum) -> {
	    DummyAuthorDummyBookCorrectedWordEntity row = new DummyAuthorDummyBookCorrectedWordEntity();
	    row.setCorrectedText(res.getString("corrected_text"));
	    row.setId(res.getInt("id"));
	    row.setIgnored(res.getBoolean("ignored"));
	    row.setOcrWordId(res.getInt("ocr_word_id"));
	    UserDetails user = new UserDetails();
	    user.setId(res.getInt("user_id"));
	    row.setUser(user);
	    return row;
	};
    }

    private DummyAuthorDummyBookOcrWordEntity getOcrWord(int bookId, int pageImageId, int x1, int y1, int x2,
	    int y2, float confidence, String rawText, int wordSequenceId) {
	DummyAuthorDummyBookOcrWordEntity ocrWord = new DummyAuthorDummyBookOcrWordEntity();
	ocrWord.setX1(x1);
	ocrWord.setY1(y1);
	ocrWord.setX2(x2);
	ocrWord.setY2(y2);
	ocrWord.setConfidence(confidence);
	ocrWord.setRawText(rawText);
	ocrWord.setOcrWordId(new OcrWordId(bookId, pageImageId, wordSequenceId));
	return ocrWord;
    }

}
