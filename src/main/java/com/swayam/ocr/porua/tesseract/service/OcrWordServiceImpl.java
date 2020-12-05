package com.swayam.ocr.porua.tesseract.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntity;
import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntity;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.UserRole;
import com.swayam.ocr.porua.tesseract.repo.CorrectedWordRepository;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepository;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordOutputDto;

@Service
public class OcrWordServiceImpl implements OcrWordService {

    private final OcrWordRepository ocrWordRepository;
    private final CorrectedWordRepository correctedWordRepository;

    public OcrWordServiceImpl(OcrWordRepository ocrWordRepository, CorrectedWordRepository correctedWordRepository) {
	this.ocrWordRepository = ocrWordRepository;
	this.correctedWordRepository = correctedWordRepository;
    }

    @Override
    public int getWordCount(long bookId, long pageImageId) {
	return ocrWordRepository.countByOcrWordIdBookIdAndOcrWordIdPageImageId(bookId, pageImageId);
    }

    @Override
    public Collection<OcrWordOutputDto> getWords(long bookId, long pageImageId, UserDetails userDetails) {
	Collection<? extends OcrWordEntityTemplate> entities = ocrWordRepository.findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(bookId, pageImageId);
	return entities.stream().map(entity -> {
	    OcrWordOutputDto dto = new OcrWordOutputDto();
	    BeanUtils.copyProperties(entity, dto);
	    List<? extends CorrectedWordEntityTemplate> correctedWords = entity.getCorrectedWords();
	    if (correctedWords.size() > 0) {

		boolean isIgnored =
			correctedWords.stream().filter(correctedWord -> (correctedWord.getUser().getRole() == UserRole.ADMIN_ROLE) || (correctedWord.getUser().getId() == userDetails.getId()))
				.anyMatch(CorrectedWordEntityTemplate::isIgnored);

		if (isIgnored) {
		    dto.setIgnored(true);
		} else {
		    Optional<? extends CorrectedWordEntityTemplate> correctedWordWithText = correctedWords.stream().filter(correctedWord -> correctedWord.getCorrectedText() != null).findFirst();
		    if (correctedWordWithText.isPresent()) {
			dto.setCorrectedText(correctedWordWithText.get().getCorrectedText());
		    }
		}

	    }
	    return dto;
	}).filter(correctedWord -> !correctedWord.isIgnored()).collect(Collectors.toList());
    }

    @Override
    public OcrWordEntityTemplate getWord(OcrWordId ocrWordId) {
	return ocrWordRepository.findByOcrWordId(ocrWordId).get();
    }

    @Transactional
    @Override
    public int markWordAsIgnored(OcrWordId ocrWordId, UserDetails user) {
	OcrWordEntityTemplate ocrWord = getWord(ocrWordId);

	Optional<CorrectedWordEntityTemplate> existingCorrection = correctedWordRepository.findByOcrWordAndUser((OcrWordEntity) ocrWord, user);

	if (existingCorrection.isPresent()) {
	    return correctedWordRepository.markAsIgnored((OcrWordEntity) ocrWord, user);
	}

	CorrectedWordEntity correctedWord = new CorrectedWordEntity();
	correctedWord.setIgnored(true);
	correctedWord.setOcrWord((OcrWordEntity) ocrWord);
	correctedWord.setUser(user);

	correctedWordRepository.save(correctedWord);

	return 1;
    }

    @Override
    public OcrWord addOcrWord(OcrWord ocrWord) {
	// TODO:: make this into a proxy
	OcrWordEntity entity = new OcrWordEntity();
	BeanUtils.copyProperties(ocrWord, entity);
	return ocrWordRepository.save(entity);
    }

    @Transactional
    @Override
    public int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user) {

	OcrWordEntityTemplate ocrWord = getWord(ocrWordId);

	Optional<CorrectedWordEntityTemplate> existingCorrection = correctedWordRepository.findByOcrWordAndUser((OcrWordEntity) ocrWord, user);

	if (existingCorrection.isPresent()) {
	    return correctedWordRepository.updateCorrectedText((OcrWordEntity) ocrWord, correctedText, user);
	}

	CorrectedWordEntity correctedWord = new CorrectedWordEntity();
	correctedWord.setCorrectedText(correctedText);
	correctedWord.setIgnored(false);
	correctedWord.setOcrWord((OcrWordEntity) ocrWord);
	correctedWord.setUser(user);

	correctedWordRepository.save(correctedWord);

	return 1;
    }

}
