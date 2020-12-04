package com.swayam.ocr.porua.tesseract.repo;

import java.util.List;
import java.util.Optional;

import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;

public interface OcrWordRepositoryTemplate {

    Optional<? extends OcrWordEntityTemplate> findByOcrWordId(OcrWordId ocrWordId);

    int countByOcrWordIdBookIdAndOcrWordIdPageImageId(long bookId, long pageImageId);

    List<? extends OcrWordEntityTemplate> findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(long bookId, long pageImageId);

    OcrWordEntityTemplate save(OcrWordEntityTemplate ocrWord);

}
