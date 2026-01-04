package de.fherling.ocr;

import com.amazon.textract.pdf.ImageType;
import com.amazon.textract.pdf.PDFDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Processes PDF documents and creates searchable PDFs with OCR text overlay.
 * Modernized with Java 21 features including var keyword and try-with-resources.
 */
public class OcrPdfFromLocalPdf {

    private static final int DPI = 300;
    private static final String IMAGE_FORMAT = "jpeg";
    
    private final OcrTextextractor ocrTextextractor = new OcrTextextractor();

    public void doOcr(String documentName, String outputDocumentName) throws IOException {
        System.out.println("Generating searchable PDF from: " + documentName);

        var pdfDocument = new PDFDocument();

        // Load PDF document and process each page as image
        try (var inputDocument = Loader.loadPDF(new RandomAccessReadBufferedFile(new File(documentName)))) {
            var pdfRenderer = new org.apache.pdfbox.rendering.PDFRenderer(inputDocument);
            var totalPages = inputDocument.getNumberOfPages();
            
            System.out.println("Processing " + totalPages + " page(s)...");
            
            for (int page = 0; page < totalPages; page++) {
                // Render image at high DPI for better OCR quality
                var image = pdfRenderer.renderImageWithDPI(page, DPI, 
                    org.apache.pdfbox.rendering.ImageType.RGB);

                // Convert image to bytes for AWS Textract
                var byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIOUtil.writeImage(image, IMAGE_FORMAT, byteArrayOutputStream);
                byteArrayOutputStream.flush();
                var imageBytes = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

                // Extract text using OCR
                var lines = ocrTextextractor.extractText(imageBytes);

                // Add extracted text to PDF page
                pdfDocument.addPage(image, ImageType.JPEG, lines);

                System.out.println("Processed page " + (page + 1) + " of " + totalPages);
            }

            // Save PDF to local disk
            try (var outputStream = new FileOutputStream(outputDocumentName)) {
                pdfDocument.save(outputStream);
            } finally {
                pdfDocument.close();
            }
        }

        System.out.println("Successfully generated searchable PDF: " + outputDocumentName);
    }
}
