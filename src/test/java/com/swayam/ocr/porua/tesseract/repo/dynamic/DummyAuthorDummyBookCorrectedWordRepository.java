package com.swayam.ocr.porua.tesseract.repo.dynamic;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.dynamic.DummyAuthorDummyBookCorrectedWordEntity;
import com.swayam.ocr.porua.tesseract.repo.CorrectedWordRepositoryTemplate;

@Repository("com.swayam.ocr.porua.tesseract.repo.dynamic.DummyAuthorDummyBookCorrectedWordRepository")
public interface DummyAuthorDummyBookCorrectedWordRepository extends
	CrudRepository<DummyAuthorDummyBookCorrectedWordEntity, Long>, CorrectedWordRepositoryTemplate {

    @Override
    @Modifying
    @Query("update DummyAuthorDummyBookCorrectedWordEntity set correctedText = :correctedText where ocrWordId = :ocrWordId and user = :user")
    int updateCorrectedText(@Param("ocrWordId") long ocrWordId, @Param("correctedText") String correctedText,
	    @Param("user") UserDetails user);

    @Override
    @Modifying
    @Query("update DummyAuthorDummyBookCorrectedWordEntity set ignored = TRUE where ocrWordId = :ocrWordId and user = :user")
    int markAsIgnored(@Param("ocrWordId") long ocrWordId, @Param("user") UserDetails user);

}
