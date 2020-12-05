package com.swayam.ocr.porua.tesseract.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.swayam.ocr.porua.tesseract.model.OcrWordEntity;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;

public interface OcrWordRepository extends CrudRepository<OcrWordEntity, Long> {

    Optional<OcrWordEntity> findByOcrWordId(OcrWordId ocrWordId);

    int countByOcrWordIdBookIdAndOcrWordIdPageImageId(long bookId, long pageImageId);

    List<OcrWordEntity> findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(long bookId, long pageImageId);

}
