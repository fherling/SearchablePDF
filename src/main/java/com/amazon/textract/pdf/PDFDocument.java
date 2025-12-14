package com.amazon.textract.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * PDF document wrapper for creating searchable PDFs with OCR text overlay.
 * Modernized with Java 21 features including var keyword and records.
 */
public class PDFDocument {

    private static final Standard14Fonts.FontName FONT_NAME = Standard14Fonts.FontName.COURIER;
    private final PDFont font = new PDType1Font(FONT_NAME);
    private final PDDocument document;

    public PDFDocument() {
        this.document = new PDDocument();
    }

    public PDFDocument(String path) throws IOException {
        this.document = Loader.loadPDF(new RandomAccessReadBufferedFile(new File(path)));
    }

    public void addText(int pageIndex, List<TextLine> lines) throws IOException {
        var page = document.getPage(pageIndex);
        var height = page.getMediaBox().getHeight();
        var width = page.getMediaBox().getWidth();

        try (var contentStream = new PDPageContentStream(document, page, 
                PDPageContentStream.AppendMode.APPEND, false)) {
            contentStream.setRenderingMode(RenderingMode.NEITHER);

            for (var textLine : lines) {
                var fontInfo = calculateFontSize(
                    textLine.text(),
                    (float) textLine.width() * width,
                    (float) textLine.height() * height
                );

                contentStream.beginText();
                contentStream.setFont(this.font, fontInfo.fontSize());
                contentStream.newLineAtOffset(
                    (float) textLine.left() * width,
                    height - height * (float) textLine.top() - fontInfo.textHeight()
                );
                contentStream.showText(textLine.text());
                contentStream.endText();
            }
        }
    }

    private FontInfo calculateFontSize(String text, float bbWidth, float bbHeight) throws IOException {
        int fontSize = 17;
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize;

        // Adjust font size to fit within bounding box width
        if (textWidth > bbWidth) {
            while (textWidth > bbWidth && fontSize > 1) {
                fontSize--;
                textWidth = font.getStringWidth(text) / 1000f * fontSize;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize;
            }
        } else if (textWidth < bbWidth) {
            while (textWidth < bbWidth) {
                fontSize++;
                textWidth = font.getStringWidth(text) / 1000f * fontSize;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize;
            }
        }

        return new FontInfo(fontSize, textHeight, textWidth);
    }

    public void addPage(BufferedImage image, ImageType imageType, List<TextLine> lines) throws IOException {
        var width = (float) image.getWidth();
        var height = (float) image.getHeight();

        var box = new PDRectangle(width, height);
        var page = new PDPage(box);
        page.setMediaBox(box);
        this.document.addPage(page);

        // Create PDF image based on type
        var pdImage = switch (imageType) {
            case JPEG -> JPEGFactory.createFromImage(this.document, image);
            case PNG -> LosslessFactory.createFromImage(this.document, image);
        };

        try (var contentStream = new PDPageContentStream(document, page)) {
            contentStream.drawImage(pdImage, 0, 0);
            contentStream.setRenderingMode(RenderingMode.NEITHER);

            for (var textLine : lines) {
                var fontInfo = calculateFontSize(
                    textLine.text(),
                    (float) textLine.width() * width,
                    (float) textLine.height() * height
                );
                
                contentStream.beginText();
                contentStream.setFont(this.font, fontInfo.fontSize());
                contentStream.newLineAtOffset(
                    (float) textLine.left() * width,
                    height - height * (float) textLine.top() - fontInfo.textHeight()
                );
                contentStream.showText(textLine.text());
                contentStream.endText();
            }
        }
    }

    public void save(String path) throws IOException {
        this.document.save(new File(path));
    }

    public void save(OutputStream os) throws IOException {
        this.document.save(os);
    }

    public void close() throws IOException {
        this.document.close();
    }
}
