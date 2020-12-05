package com.swayam.ocr.porua.tesseract.service;

import java.util.Collection;

import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordOutputDto;

public interface OcrWordService {

    int getWordCount(long bookId, long rawImageId);

    Collection<OcrWordOutputDto> getWords(long bookId, long pageImageId, UserDetails userDetails);

    OcrWord addOcrWord(OcrWord rawOcrWord);

    int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user);

    OcrWord getWord(OcrWordId ocrWordId);

    int markWordAsIgnored(OcrWordId ocrWordId, UserDetails user);

}
