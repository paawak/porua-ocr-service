package com.swayam.ocr.porua.tesseract.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrCorrection;

import lombok.Data;

@Entity
@Table(name = "corrected_word")
@Data
public class CorrectedWord implements OcrCorrection {

    @EmbeddedId
    private OcrWordId ocrWordId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserDetails user;

    @Column(name = "corrected_text")
    private String correctedText;

    @Column
    private boolean ignored;

}
