package com.swayam.ocr.porua.tesseract.model;

public interface CorrectedWord {

    UserDetails getUser();

    String getCorrectedText();

    boolean isIgnored();

}
