package com.swayam.ocr.porua.tesseract.rest.train.dto;

import com.swayam.ocr.porua.tesseract.model.OcrWordId;

public interface OcrCorrection {

    OcrWordId getOcrWordId();

    void setOcrWordId(OcrWordId ocrWordId);

    String getCorrectedText();

    void setCorrectedText(String correctedText);

}