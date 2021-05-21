package com.swayam.ocr.porua.tesseract.repo.dynamic;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntity;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepositoryTemplate;

@Repository("com.swayam.ocr.porua.tesseract.repo.dynamic.DummyAuthorDummyBookOcrWordRepository")
public interface DummyAuthorDummyBookOcrWordRepository
	extends CrudRepository<OcrWordEntity, Long>, OcrWordRepositoryTemplate {

    @Override
    Optional<OcrWord> findByOcrWordId(OcrWordId ocrWordId);

    @Override
    int countByOcrWordIdBookIdAndOcrWordIdPageImageId(long bookId, long pageImageId);

    @Override
    List<OcrWord> findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(long bookId,
	    long pageImageId);

}
