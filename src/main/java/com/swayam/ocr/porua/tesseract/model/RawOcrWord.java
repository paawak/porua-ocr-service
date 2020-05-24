package com.swayam.ocr.porua.tesseract.model;

import lombok.Value;

@Value
public class RawOcrWord {

    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final float confidence;
    private final String text;
    private final int wordSequenceNumber;

}
