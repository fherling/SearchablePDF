package de.fherling.ocr;

import com.amazon.textract.pdf.TextLine;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Extracts text from images using Amazon Textract service.
 * Modernized with Java 21 features including streams and functional programming.
 */
public class OcrTextextractor {
    
    /**
     * Extract text from image using Amazon Textract
     * @param imageBytes the image data as ByteBuffer
     * @return List of TextLine objects containing extracted text with positioning
     */
    public List<TextLine> extractText(ByteBuffer imageBytes) {
        try (TextractClient client = TextractClient.builder().build()) {
            var request = DetectDocumentTextRequest.builder()
                    .document(Document.builder()
                            .bytes(SdkBytes.fromByteBuffer(imageBytes))
                            .build())
                    .build();

            var result = client.detectDocumentText(request);

            // Use streams and functional programming for cleaner code
            return result.blocks().stream()
                    .filter(block -> block.blockType() == BlockType.LINE)
                    .map(this::convertBlockToTextLine)
                    .toList();
        }
    }
    
    /**
     * Converts a Textract Block to a TextLine record
     */
    private TextLine convertBlockToTextLine(Block block) {
        var boundingBox = block.geometry().boundingBox();
        return new TextLine(
                boundingBox.left(),
                boundingBox.top(),
                boundingBox.width(),
                boundingBox.height(),
                block.text()
        );
    }
}
