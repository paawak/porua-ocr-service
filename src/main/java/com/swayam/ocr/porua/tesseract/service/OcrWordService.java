package com.swayam.ocr.porua.tesseract.service;

import java.util.Collection;

import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordOutputDto;

public interface OcrWordService {

    int getWordCount(long bookId, long rawImageId);

    Collection<OcrWordOutputDto> getWords(long bookId, long pageImageId, UserDetails userDetails);

    OcrWordEntityTemplate addOcrWord(OcrWordEntityTemplate rawOcrWord);

    int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user);

    OcrWordEntityTemplate getWord(OcrWordId ocrWordId);

    int markWordAsIgnored(OcrWordId ocrWordId, UserDetails user);

}
