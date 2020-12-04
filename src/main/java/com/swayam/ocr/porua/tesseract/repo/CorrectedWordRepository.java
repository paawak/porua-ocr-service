package com.swayam.ocr.porua.tesseract.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntity;
import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntity;
import com.swayam.ocr.porua.tesseract.model.UserDetails;

public interface CorrectedWordRepository extends CrudRepository<CorrectedWordEntity, Long>, CorrectedWordRepositoryTemplate {

    @Override
    Optional<CorrectedWordEntityTemplate> findByOcrWordAndUser(OcrWordEntity ocrWord, UserDetails user);

    @Override
    @Modifying
    @Query("update CorrectedWordEntity set correctedText = :correctedText where ocrWord = :ocrWord and user = :user")
    int updateCorrectedText(@Param("ocrWord") OcrWordEntity ocrWord, @Param("correctedText") String correctedText, @Param("user") UserDetails user);

    @Override
    @Modifying
    @Query("update CorrectedWordEntity set ignored = TRUE where ocrWord = :ocrWord and user = :user")
    int markAsIgnored(@Param("ocrWord") OcrWordEntity ocrWord, @Param("user") UserDetails user);

}
