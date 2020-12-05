package com.swayam.ocr.porua.tesseract.repo;

import java.util.Optional;

import com.swayam.ocr.porua.tesseract.model.CorrectedWord;
import com.swayam.ocr.porua.tesseract.model.UserDetails;

public interface CorrectedWordRepositoryTemplate {

    Optional<CorrectedWord> findByOcrWordIdAndUser(long ocrWordId, UserDetails user);

    int updateCorrectedText(long ocrWordId, String correctedText, UserDetails user);

    int markAsIgnored(long ocrWordId, UserDetails user);

    CorrectedWord save(CorrectedWord entity);

}
