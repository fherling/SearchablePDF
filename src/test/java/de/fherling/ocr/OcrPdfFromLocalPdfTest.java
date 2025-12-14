package de.fherling.ocr;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OcrPdfFromLocalPdfTest {

    OcrPdfFromLocalPdf cut = new OcrPdfFromLocalPdf();

    @Test
    @Disabled("Only for manual testing")
    void doOcr() throws IOException {


        Files.deleteIfExists(Path.of("target/ocr-test-result.pdf"));

        cut.doOcr("src/test/resources/ocr-test.pdf", "target/ocr-test-result.pdf");

        assertTrue(Files.exists(Path.of("target/ocr-test-result.pdf")));

    }
}