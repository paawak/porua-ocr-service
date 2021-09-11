package com.swayam.ocr.porua.tesseract.repo.dynamic;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.swayam.ocr.porua.tesseract.model.dynamic.RajshekharBasuMahabharatBanglaOcrWordEntity;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepositoryTemplate;

@Repository("com.swayam.ocr.porua.tesseract.repo.dynamic.RajshekharBasuMahabharatBanglaOcrWordRepository")
public interface RajshekharBasuMahabharatBanglaOcrWordRepository extends CrudRepository<RajshekharBasuMahabharatBanglaOcrWordEntity, Long>, OcrWordRepositoryTemplate {

}
