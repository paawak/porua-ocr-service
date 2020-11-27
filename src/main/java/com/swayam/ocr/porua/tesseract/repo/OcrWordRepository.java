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

    int countByIgnoredFalseAndOcrWordIdBookIdAndOcrWordIdPageImageId(long bookId, long pageImageId);

    List<OcrWord> findByIgnoredFalseAndOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(long bookId, long pageImageId);

    @Modifying
    @Query("update OcrWord set ignored = TRUE where ocrWordId = :ocrWordId")
    int markAsIgnored(@Param("ocrWordId") OcrWordId ocrWordId);

    @Modifying
    @Query("update OcrWord set correctedText = :correctedText where ocrWordId = :ocrWordId")
    int updateCorrectedText(@Param("ocrWordId") OcrWordId ocrWordId, @Param("correctedText") String correctedText);

    void deleteByOcrWordId(OcrWordId ocrWordId);

}
