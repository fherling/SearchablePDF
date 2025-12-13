package com.amazon.textract.pdf;

/**
 * Font information for rendering text in PDF.
 * Using Java 21 record for immutability and automatic equals/hashCode/toString.
 */
public record FontInfo(
    int fontSize,
    float textHeight,
    float textWidth
) {}

