package com.swayam.ocr.porua.tesseract.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrCorrection;

import lombok.Data;

@Entity
@Table(name = "corrected_word")
@Data
public class CorrectedWord implements OcrCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserDetails user;

    @JsonManagedReference
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ocr_word_id")
    private OcrWord ocrWord;

    @Column(name = "corrected_text")
    private String correctedText;

    @Column
    private boolean ignored;

    @Override
    public OcrWordId getOcrWordId() {
	return ocrWord.getOcrWordId();
    }

}
