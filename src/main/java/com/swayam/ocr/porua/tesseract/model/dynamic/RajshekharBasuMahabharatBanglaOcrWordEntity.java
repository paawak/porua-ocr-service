package com.swayam.ocr.porua.tesseract.model.dynamic;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;

@Entity
@Table(name = "rajshekhar_basu_mahabharat_bangla_ocr_word")
public class RajshekharBasuMahabharatBanglaOcrWordEntity extends OcrWordEntityTemplate {

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "ocrWordId")
    private List<RajshekharBasuMahabharatBanglaCorrectedWordEntity> correctedWords;

    @Override
    public List<RajshekharBasuMahabharatBanglaCorrectedWordEntity> getCorrectedWords() {
	return correctedWords;
    }

}
