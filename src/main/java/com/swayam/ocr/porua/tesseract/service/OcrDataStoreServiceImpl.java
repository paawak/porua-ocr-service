package com.swayam.ocr.porua.tesseract.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.swayam.ocr.porua.tesseract.model.Book;
import com.swayam.ocr.porua.tesseract.model.CorrectedWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntity;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.PageImage;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.UserRole;
import com.swayam.ocr.porua.tesseract.repo.BookRepository;
import com.swayam.ocr.porua.tesseract.repo.CorrectedWordRepository;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepository;
import com.swayam.ocr.porua.tesseract.repo.PageImageRepository;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordOutputDto;

@Service
public class OcrDataStoreServiceImpl implements OcrDataStoreService {

    private final BookRepository bookRepository;
    private final PageImageRepository pageImageRepository;
    private final OcrWordRepository ocrWordRepository;
    private final CorrectedWordRepository correctedWordRepository;

    public OcrDataStoreServiceImpl(BookRepository bookRepository, PageImageRepository pageImageRepository, OcrWordRepository ocrWordRepository, CorrectedWordRepository correctedWordRepository) {
	this.bookRepository = bookRepository;
	this.pageImageRepository = pageImageRepository;
	this.ocrWordRepository = ocrWordRepository;
	this.correctedWordRepository = correctedWordRepository;
    }

    @Override
    public Book addBook(Book book) {
	return bookRepository.save(book);
    }

    @Override
    public List<Book> getBooks() {
	return StreamSupport.stream(bookRepository.findAll().spliterator(), false).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Book getBook(long bookId) {
	return bookRepository.findById(bookId).get();
    }

    @Override
    public PageImage addPageImage(PageImage pageImage) {
	return pageImageRepository.save(pageImage);
    }

    @Override
    public PageImage getPageImage(long pageImageId) {
	return pageImageRepository.findById(pageImageId).get();
    }

    @Override
    public int getPageCount(long bookId) {
	return pageImageRepository.countByBookId(bookId);
    }

    @Override
    public List<PageImage> getPages(long bookId) {
	return pageImageRepository.findByBookIdAndIgnoredIsFalseAndCorrectionCompletedIsFalseOrderById(bookId);
    }

    @Override
    public int getWordCount(long bookId, long pageImageId) {
	return ocrWordRepository.countByOcrWordIdBookIdAndOcrWordIdPageImageId(bookId, pageImageId);
    }

    @Override
    public Collection<OcrWordOutputDto> getWords(long bookId, long pageImageId, UserDetails userDetails) {
	Collection<OcrWordEntity> entities = ocrWordRepository.findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(bookId, pageImageId);
	return entities.stream().map(entity -> {
	    OcrWordOutputDto dto = new OcrWordOutputDto();
	    BeanUtils.copyProperties(entity, dto);
	    List<CorrectedWord> correctedWords = entity.getCorrectedWords();
	    if (correctedWords.size() > 0) {

		boolean isIgnored = correctedWords.stream().filter(correctedWord -> (correctedWord.getUser().getRole() == UserRole.ADMIN_ROLE) || correctedWord.getUser().equals(userDetails))
			.anyMatch(CorrectedWord::isIgnored);

		if (isIgnored) {
		    dto.setIgnored(true);
		} else {
		    Optional<CorrectedWord> correctedWordWithText = correctedWords.stream().filter(correctedWord -> correctedWord.getCorrectedText() != null).findFirst();
		    if (correctedWordWithText.isPresent()) {
			dto.setCorrectedText(correctedWordWithText.get().getCorrectedText());
		    }
		}

	    }
	    return dto;
	}).collect(Collectors.toList());
    }

    @Override
    public OcrWordEntity getWord(OcrWordId ocrWordId) {
	return ocrWordRepository.findByOcrWordId(ocrWordId).get();
    }

    @Transactional
    @Override
    public int markWordAsIgnored(OcrWordId ocrWordId, UserDetails user) {
	OcrWordEntity ocrWord = getWord(ocrWordId);

	Optional<CorrectedWord> existingCorrection = correctedWordRepository.findByOcrWordAndUser(ocrWord, user);

	if (existingCorrection.isPresent()) {
	    return correctedWordRepository.markAsIgnored(ocrWord, user);
	}

	CorrectedWord correctedWord = new CorrectedWord();
	correctedWord.setIgnored(true);
	correctedWord.setOcrWord(ocrWord);
	correctedWord.setUser(user);

	correctedWordRepository.save(correctedWord);

	return 1;
    }

    @Override
    public OcrWordEntity addOcrWord(OcrWordEntity ocrWord) {
	return ocrWordRepository.save(ocrWord);
    }

    @Transactional
    @Override
    public int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user) {

	OcrWordEntity ocrWord = getWord(ocrWordId);

	Optional<CorrectedWord> existingCorrection = correctedWordRepository.findByOcrWordAndUser(ocrWord, user);

	if (existingCorrection.isPresent()) {
	    return correctedWordRepository.updateCorrectedText(ocrWord, correctedText, user);
	}

	CorrectedWord correctedWord = new CorrectedWord();
	correctedWord.setCorrectedText(correctedText);
	correctedWord.setIgnored(false);
	correctedWord.setOcrWord(ocrWord);
	correctedWord.setUser(user);

	correctedWordRepository.save(correctedWord);

	return 1;
    }

    @Transactional
    @Override
    public int markPageAsIgnored(long pageImageId) {
	return pageImageRepository.markPageAsIgnored(pageImageId);
    }

    @Transactional
    @Override
    public int markPageAsCorrectionCompleted(long pageImageId) {
	return pageImageRepository.markPageAsCorrectionCompleted(pageImageId);
    }

}
