package com.swayam.ocr.porua.tesseract.rest.train.dto;

public interface OcrWordDto extends OcrWordIdentifier {

    String getRawText();

    int getX1();

    int getY1();

    int getX2();

    int getY2();

    float getConfidence();

    Integer getLineNumber();

}