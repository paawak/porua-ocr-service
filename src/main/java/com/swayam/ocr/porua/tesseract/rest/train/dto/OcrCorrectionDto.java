package com.swayam.ocr.porua.tesseract.rest.train.dto;

import com.swayam.ocr.porua.tesseract.model.OcrWordId;

import lombok.Data;

@Data
public class OcrCorrectionDto implements OcrCorrection {

    private OcrWordId ocrWordId;

    private String correctedText;

}
