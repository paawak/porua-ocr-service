package com.swayam.ocr.porua.tesseract.model;

import java.util.List;

public interface OcrWord {

    OcrWordId getOcrWordId();

    String getRawText();

    int getX1();

    int getY1();

    int getX2();

    int getY2();

    float getConfidence();

    Integer getLineNumber();

    List<? extends CorrectedWordEntityTemplate> getCorrectedWords();

}