package de.fherling.ocr;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OcrPdfFromLocalPdfTest {

    OcrPdfFromLocalPdf cut = new OcrPdfFromLocalPdf();

    @Test
    void doOcr() throws IOException {

        cut.doOcr("src/test/resources/ocr-test.pdf", "target/ocr-test-result.pdf");

        File file = new File("target/ocr-test-result.pdf");
        assertTrue(file.exists());

    }
}