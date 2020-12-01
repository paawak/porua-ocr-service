package com.swayam.ocr.porua.tesseract.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.swayam.ocr.porua.tesseract.model.CorrectedWord;
import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.UserDetails;

public interface CorrectedWordRepository extends CrudRepository<CorrectedWord, Long> {

    Optional<CorrectedWord> findByOcrWordAndUser(OcrWord ocrWord, UserDetails user);

    @Modifying
    @Query("update CorrectedWord set correctedText = :correctedText, user = :user where ocrWord = :ocrWord")
    int updateCorrectedText(@Param("ocrWord") OcrWord ocrWord, @Param("correctedText") String correctedText, @Param("user") UserDetails user);

}
