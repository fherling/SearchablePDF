package com.amazon.textract.pdf;

/**
 * Represents a line of text extracted from a document, including its position and dimensions.
 * Using Java 21 record for immutability and automatic equals/hashCode/toString.
 */
public record TextLine(
    double left,
    double top,
    double width,
    double height,
    String text
) {}