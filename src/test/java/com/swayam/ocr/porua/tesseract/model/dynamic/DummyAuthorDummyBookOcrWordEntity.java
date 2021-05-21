package com.swayam.ocr.porua.tesseract.model.dynamic;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntity;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;

import lombok.Getter;

@Getter
@Entity
@Table(name = "dummy_author_dummy_book_ocr_word")
public class DummyAuthorDummyBookOcrWordEntity extends OcrWordEntityTemplate {

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ocrWordId")
    private List<CorrectedWordEntity> correctedWords;

}
