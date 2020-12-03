package com.swayam.ocr.porua.tesseract.model;

public interface OcrWordWithCorrection {

    String getCorrectedText();

    boolean isIgnored();

}
