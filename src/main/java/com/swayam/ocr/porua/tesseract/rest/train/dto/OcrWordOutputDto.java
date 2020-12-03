package com.swayam.ocr.porua.tesseract.rest.train.dto;

import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.OcrWordWithCorrection;

import lombok.Data;

@Data
public class OcrWordOutputDto implements OcrWord, OcrWordWithCorrection {

    private OcrWordId ocrWordId;

    private String rawText;

    private int x1;

    private int y1;

    private int x2;

    private int y2;

    private float confidence;

    private Integer lineNumber;

    private String correctedText;

    private boolean ignored;

}
