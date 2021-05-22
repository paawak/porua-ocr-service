package com.swayam.ocr.porua.tesseract.repo.dynamic;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.swayam.ocr.porua.tesseract.model.dynamic.DummyAuthorDummyBookOcrWordEntity;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepositoryTemplate;

@Repository("com.swayam.ocr.porua.tesseract.repo.dynamic.DummyAuthorDummyBookOcrWordRepository")
public interface DummyAuthorDummyBookOcrWordRepository
	extends CrudRepository<DummyAuthorDummyBookOcrWordEntity, Long>, OcrWordRepositoryTemplate {

}
