package com.swayam.ocr.porua.tesseract.repo;

import java.util.Optional;

import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntity;
import com.swayam.ocr.porua.tesseract.model.UserDetails;

public interface CorrectedWordRepositoryTemplate {

    Optional<CorrectedWordEntityTemplate> findByOcrWordAndUser(OcrWordEntity ocrWord, UserDetails user);

    int updateCorrectedText(OcrWordEntity ocrWord, String correctedText, UserDetails user);

    int markAsIgnored(OcrWordEntity ocrWord, UserDetails user);

    CorrectedWordEntityTemplate save(CorrectedWordEntityTemplate correctedWord);

}
