package com.swayam.ocr.porua.tesseract.repo;

import java.util.List;
import java.util.Optional;

import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;

public interface OcrWordRepositoryTemplate {

    Optional<OcrWord> findByOcrWordId(OcrWordId ocrWordId);

    int countByOcrWordIdBookIdAndOcrWordIdPageImageId(long bookId, long pageImageId);

    List<OcrWord> findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(long bookId, long pageImageId);

    OcrWord save(OcrWord entity);

}
