package com.swayam.ocr.porua.tesseract.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.OcrWordTemplate;

@NoRepositoryBean
public interface OcrWordRepositoryTemplate<T extends OcrWordTemplate> extends CrudRepository<T, Long> {

    Optional<T> findByOcrWordId(OcrWordId ocrWordId);

    int countByOcrWordIdBookIdAndOcrWordIdPageImageId(long bookId, long pageImageId);

    List<T> findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(long bookId, long pageImageId);

    @Modifying
    @Query("update OcrWord set ignored = TRUE where ocrWordId = :ocrWordId")
    int markAsIgnored(@Param("ocrWordId") OcrWordId ocrWordId);

    void deleteByOcrWordId(OcrWordId ocrWordId);

}
