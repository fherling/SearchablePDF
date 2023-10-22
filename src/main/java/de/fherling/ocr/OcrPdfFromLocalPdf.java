package de.fherling.ocr;

import com.amazon.textract.pdf.ImageType;
import com.amazon.textract.pdf.PDFDocument;
import com.amazon.textract.pdf.TextLine;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;

public class OcrPdfFromLocalPdf {

    private final OcrTextextractor ocrTextextractor = new OcrTextextractor();

    public void doOcr(String documentName, String outputDocumentName) throws IOException {

        System.out.println("Generating searchable pdf from: " + documentName);

        PDFDocument pdfDocument = new PDFDocument();

        List<TextLine> lines;
        BufferedImage image;
        ByteArrayOutputStream byteArrayOutputStream;
        ByteBuffer imageBytes;

        //Load pdf document and process each page as image
        try(PDDocument inputDocument = Loader.loadPDF(new RandomAccessReadBufferedFile(new File(documentName)))) {
            PDFRenderer pdfRenderer = new PDFRenderer(inputDocument);
            for (int page = 0; page < inputDocument.getNumberOfPages(); ++page) {

                //Render image
                image = pdfRenderer.renderImageWithDPI(page, 300, org.apache.pdfbox.rendering.ImageType.RGB);

                //Get image bytes
                byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIOUtil.writeImage(image, "jpeg", byteArrayOutputStream);
                byteArrayOutputStream.flush();
                imageBytes = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

                //Extract text
                lines = ocrTextextractor.extractText(imageBytes);

                //Add extracted text to pdf page
                pdfDocument.addPage(image, ImageType.JPEG, lines);

                System.out.println("Processed page index: " + page);
            }

            //Save PDF to local disk
            try (OutputStream outputStream = new FileOutputStream(outputDocumentName)) {
                pdfDocument.save(outputStream);
                pdfDocument.close();
            }
        }

        System.out.println("Generated searchable pdf: " + outputDocumentName);
    }
}
