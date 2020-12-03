package com.swayam.ocr.porua.tesseract.model;

public interface OcrWord {

    OcrWordId getOcrWordId();

    String getRawText();

    int getX1();

    int getY1();

    int getX2();

    int getY2();

    float getConfidence();

    Integer getLineNumber();

}