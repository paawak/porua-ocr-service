package com.swayam.ocr.porua.tesseract.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;

public interface OcrWordRepository extends CrudRepository<OcrWord, Long> {

    Optional<OcrWord> findByOcrWordId(OcrWordId ocrWordId);

    int countByOcrWordIdBookIdAndOcrWordIdPageImageId(long bookId, long pageImageId);

    List<OcrWord> findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(long bookId, long pageImageId);

    @Modifying
    @Query("update OcrWord set ignored = TRUE where ocrWordId = :ocrWordId")
    int markAsIgnored(@Param("ocrWordId") OcrWordId ocrWordId);

    void deleteByOcrWordId(OcrWordId ocrWordId);

}
