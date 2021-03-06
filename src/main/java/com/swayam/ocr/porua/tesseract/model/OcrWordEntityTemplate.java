package com.swayam.ocr.porua.tesseract.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public abstract class OcrWordEntityTemplate implements OcrWord {

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

}
