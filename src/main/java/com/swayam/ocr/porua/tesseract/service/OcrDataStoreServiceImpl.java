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
import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.PageImage;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.repo.BookRepository;
import com.swayam.ocr.porua.tesseract.repo.CorrectedWordRepository;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepository;
import com.swayam.ocr.porua.tesseract.repo.PageImageRepository;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordDtoImpl;

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
    public Collection<OcrWordDtoImpl> getWords(long bookId, long pageImageId) {
	Collection<OcrWord> entities = ocrWordRepository.findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(bookId, pageImageId);
	return entities.stream().map(entity -> {
	    OcrWordDtoImpl dto = new OcrWordDtoImpl();
	    BeanUtils.copyProperties(entity, dto);
	    List<CorrectedWord> correctedWords = entity.getCorrectedWords();
	    if (correctedWords.size() > 0) {
		dto.setCorrectedText(correctedWords.get(0).getCorrectedText());
	    }
	    return dto;
	}).collect(Collectors.toList());
    }

    @Override
    public OcrWord getWord(OcrWordId ocrWordId) {
	return ocrWordRepository.findByOcrWordId(ocrWordId).get();
    }

    @Transactional
    @Override
    public int markWordAsIgnored(OcrWordId ocrWordId) {
	return ocrWordRepository.markAsIgnored(ocrWordId);
    }

    @Override
    public OcrWord addOcrWord(OcrWord ocrWord) {
	return ocrWordRepository.save(ocrWord);
    }

    @Transactional
    @Override
    public int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user) {

	OcrWord ocrWord = getWord(ocrWordId);

	Optional<CorrectedWord> existingCorrection = correctedWordRepository.findByOcrWordAndUser(ocrWord, user);

	if (existingCorrection.isPresent()) {
	    correctedWordRepository.updateCorrectedText(ocrWord, correctedText, user);
	    return 1;
	}

	CorrectedWord correctedWord = new CorrectedWord();
	correctedWord.setCorrectedText(correctedText);
	correctedWord.setIgnored(false);
	correctedWord.setOcrWord(ocrWord);
	correctedWord.setUser(user);

	correctedWordRepository.save(correctedWord);

	return 1;
    }

    @Override
    public void removeWord(OcrWordId ocrWordId) {
	ocrWordRepository.deleteByOcrWordId(ocrWordId);
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
