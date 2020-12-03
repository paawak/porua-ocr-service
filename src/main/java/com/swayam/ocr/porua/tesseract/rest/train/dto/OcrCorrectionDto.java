package com.swayam.ocr.porua.tesseract.rest.train.dto;

public interface OcrCorrectionDto extends OcrWordIdentifier {

    String getCorrectedText();

}