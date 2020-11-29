package com.swayam.ocr.porua.tesseract.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrCorrection;

import lombok.Data;

@Entity
@Table(name = "ocr_word")
@Data
public class OcrWord implements OcrCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    private OcrWordId ocrWordId;

    @Column(name = "raw_text")
    private String rawText;

    @Column
    private int x1;

    @Column
    private int y1;

    @Column
    private int x2;

    @Column
    private int y2;

    @Column
    private float confidence;

    @Column(name = "line_number")
    private Integer lineNumber;

    @JsonBackReference
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "ocrWord")
    private CorrectedWord ocrWord;

    @Override
    public String getCorrectedText() {
	return (ocrWord == null) ? null : ocrWord.getCorrectedText();
    }

}
