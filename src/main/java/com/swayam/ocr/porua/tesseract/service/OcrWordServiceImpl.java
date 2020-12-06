package com.swayam.ocr.porua.tesseract.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.swayam.ocr.porua.tesseract.model.CorrectedWord;
import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntity;
import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntity;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.UserRole;
import com.swayam.ocr.porua.tesseract.repo.BookRepository;
import com.swayam.ocr.porua.tesseract.repo.CorrectedWordRepositoryTemplate;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepositoryTemplate;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordOutputDto;

import lombok.Value;

@Service
public class OcrWordServiceImpl implements OcrWordService {

    private static final String OCR_WORD_ENTITY_SUFFIX = "OcrWordEntity";
    private static final String CORRECTED_WORD_ENTITY_SUFFIX = "CorrectedWordEntity";
    private static final String OCR_WORD_ENTITY_REPOSITORY_SUFFIX = "OcrWordRepository";
    private static final String CORRECTED_WORD_ENTITY_REPOSITORY_SUFFIX = "CorrectedWordRepository";

    private final BookRepository bookRepository;
    private final ApplicationContext applicationContext;

    public OcrWordServiceImpl(BookRepository bookRepository, ApplicationContext applicationContext) {
	this.bookRepository = bookRepository;
	this.applicationContext = applicationContext;
    }

    @Override
    public int getWordCount(long bookId, long pageImageId) {
	return getOcrWordRepositoryTemplate(bookId).countByOcrWordIdBookIdAndOcrWordIdPageImageId(bookId, pageImageId);
    }

    @Override
    public Collection<OcrWordOutputDto> getWords(long bookId, long pageImageId, UserDetails userDetails) {
	Collection<OcrWord> entities = getOcrWordRepositoryTemplate(bookId).findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(bookId, pageImageId);
	return entities.stream().map(entity -> {
	    OcrWordOutputDto dto = new OcrWordOutputDto();
	    BeanUtils.copyProperties(entity, dto);
	    List<? extends CorrectedWord> correctedWords = entity.getCorrectedWords();
	    if (correctedWords.size() > 0) {

		boolean isIgnored =
			correctedWords.stream().filter(correctedWord -> (correctedWord.getUser().getRole() == UserRole.ADMIN_ROLE) || (correctedWord.getUser().getId() == userDetails.getId()))
				.anyMatch(CorrectedWord::isIgnored);

		if (isIgnored) {
		    dto.setIgnored(true);
		} else {
		    Optional<? extends CorrectedWord> correctedWordWithText = correctedWords.stream().filter(correctedWord -> correctedWord.getCorrectedText() != null).findFirst();
		    if (correctedWordWithText.isPresent()) {
			dto.setCorrectedText(correctedWordWithText.get().getCorrectedText());
		    }
		}

	    }
	    return dto;
	}).filter(correctedWord -> !correctedWord.isIgnored()).collect(Collectors.toList());
    }

    @Override
    public OcrWord getWord(OcrWordId ocrWordId) {
	return getOcrWordRepositoryTemplate(ocrWordId.getBookId()).findByOcrWordId(ocrWordId).get();
    }

    @Transactional
    @Override
    public int markWordAsIgnored(OcrWordId ocrWordId, UserDetails user) {
	OcrWord ocrWord = getWord(ocrWordId);

	CorrectedWordRepositoryTemplate CorrectedWordRepositoryTemplate = getCorrectedWordRepositoryTemplate(ocrWordId.getBookId());
	Optional<CorrectedWord> existingCorrection = CorrectedWordRepositoryTemplate.findByOcrWordIdAndUser(ocrWord.getId(), user);

	if (existingCorrection.isPresent()) {
	    return CorrectedWordRepositoryTemplate.markAsIgnored(ocrWord.getId(), user);
	}

	CorrectedWordRepositoryTemplate.save(toEntity(Optional.empty(), ocrWord.getId(), user));

	return 1;
    }

    @Override
    public OcrWord addOcrWord(OcrWord ocrWord) {
	return getOcrWordRepositoryTemplate(ocrWord.getOcrWordId().getBookId()).save(toEntity(ocrWord));
    }

    @Transactional
    @Override
    public int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user) {

	OcrWord ocrWord = getWord(ocrWordId);

	CorrectedWordRepositoryTemplate CorrectedWordRepositoryTemplate = getCorrectedWordRepositoryTemplate(ocrWordId.getBookId());
	Optional<CorrectedWord> existingCorrection = CorrectedWordRepositoryTemplate.findByOcrWordIdAndUser(ocrWord.getId(), user);

	if (existingCorrection.isPresent()) {
	    return CorrectedWordRepositoryTemplate.updateCorrectedText(ocrWord.getId(), correctedText, user);
	}

	CorrectedWordRepositoryTemplate.save(toEntity(Optional.of(correctedText), ocrWord.getId(), user));

	return 1;
    }

    private OcrWordRepositoryTemplate getOcrWordRepositoryTemplate(long bookId) {
	EntityClassDetails entityClassDetails = getEntityClassDetails(bookId);
	// TODO; find based on name
	return applicationContext.getBean(OcrWordRepositoryTemplate.class);
    }

    private CorrectedWordRepositoryTemplate getCorrectedWordRepositoryTemplate(long bookId) {
	EntityClassDetails entityClassDetails = getEntityClassDetails(bookId);
	// TODO; find based on name
	return applicationContext.getBean(CorrectedWordRepositoryTemplate.class);
    }

    @VisibleForTesting
    EntityClassDetails getEntityClassDetails(long bookId) {
	String baseTableName = bookRepository.findById(bookId).get().getBaseTableName();

	String regex = "_[a-z]";

	Pattern pattern = Pattern.compile(regex);

	Matcher matcher = pattern.matcher(baseTableName);

	StringBuilder baseNameBuilder = new StringBuilder();

	while (matcher.find()) {
	    String originalToken = matcher.group();
	    String replacement = Character.toString(originalToken.charAt(1)).toUpperCase();
	    matcher.appendReplacement(baseNameBuilder, replacement);
	}

	matcher.appendTail(baseNameBuilder);

	String baseName = baseNameBuilder.replace(0, 1, Character.toString(baseNameBuilder.charAt(0)).toUpperCase()).toString();

	return new EntityClassDetails(baseName + OCR_WORD_ENTITY_SUFFIX, baseName + CORRECTED_WORD_ENTITY_SUFFIX, baseName + OCR_WORD_ENTITY_REPOSITORY_SUFFIX,
		baseName + CORRECTED_WORD_ENTITY_REPOSITORY_SUFFIX);
    }

    private OcrWordEntity toEntity(OcrWord ocrWord) {
	// TODO:: make this into a proxy
	OcrWordEntity entity = new OcrWordEntity();
	BeanUtils.copyProperties(ocrWord, entity);
	return entity;
    }

    private CorrectedWordEntity toEntity(Optional<String> correctedText, long ocrWordId, UserDetails user) {
	CorrectedWordEntityTemplate correctedWord = new CorrectedWordEntityTemplate();
	if (correctedText.isPresent()) {
	    correctedWord.setCorrectedText(correctedText.get());
	    correctedWord.setIgnored(false);
	} else {
	    correctedWord.setIgnored(true);
	}
	correctedWord.setOcrWordId(ocrWordId);
	correctedWord.setUser(user);
	// TODO:: make this into a proxy
	CorrectedWordEntity entity = new CorrectedWordEntity();
	BeanUtils.copyProperties(correctedWord, entity);
	return entity;
    }

    @VisibleForTesting
    @Value
    static class EntityClassDetails {

	private final String ocrWordEntity;
	private final String correctedWordEntity;
	private final String ocrWordEntityRepository;
	private final String correctedWordEntityRepository;

    }

}
