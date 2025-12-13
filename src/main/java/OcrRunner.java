import de.fherling.ocr.OcrPdfFromLocalPdf;
import software.amazon.awssdk.services.textract.model.TextractException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main entry point for OCR PDF processing application.
 * Modernized with Java 21 features and improved error handling.
 */
public class OcrRunner {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java OcrRunner <input-file> <output-file>");
            System.err.println("  input-file:  Path to the PDF file to process");
            System.err.println("  output-file: Path where the searchable PDF will be saved");
            System.exit(1);
        }

        var inputFile = args[0];
        var outputFile = args[1];

        // Validate input file exists
        if (!Files.exists(Path.of(inputFile))) {
            System.err.println("Error: Input file does not exist: " + inputFile);
            System.exit(1);
        }

        // Validate input file is readable
        if (!Files.isReadable(Path.of(inputFile))) {
            System.err.println("Error: Input file is not readable: " + inputFile);
            System.exit(1);
        }

        System.out.println("Input file: " + inputFile);
        System.out.println("Output file: " + outputFile);

        try {
            // Generate searchable PDF from local pdf
            var localPdf = new OcrPdfFromLocalPdf();
            localPdf.doOcr(inputFile, outputFile);
            System.out.println("Successfully created searchable PDF: " + outputFile);
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (TextractException e) {
            System.err.println("AWS Textract Error: " + e.getMessage());
            System.err.println("Please check your AWS credentials and permissions");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
