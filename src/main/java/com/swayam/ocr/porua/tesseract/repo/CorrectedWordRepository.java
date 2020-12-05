package com.swayam.ocr.porua.tesseract.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntity;
import com.swayam.ocr.porua.tesseract.model.OcrWordWithCorrection;
import com.swayam.ocr.porua.tesseract.model.UserDetails;

public interface CorrectedWordRepository extends CrudRepository<CorrectedWordEntity, Long> {

    Optional<OcrWordWithCorrection> findByOcrWordIdAndUser(long ocrWordId, UserDetails user);

    @Modifying
    @Query("update CorrectedWordEntity set correctedText = :correctedText where ocrWordId = :ocrWordId and user = :user")
    int updateCorrectedText(@Param("ocrWordId") long ocrWordId, @Param("correctedText") String correctedText, @Param("user") UserDetails user);

    @Modifying
    @Query("update CorrectedWordEntity set ignored = TRUE where ocrWordId = :ocrWordId and user = :user")
    int markAsIgnored(@Param("ocrWordId") long ocrWordId, @Param("user") UserDetails user);

}
