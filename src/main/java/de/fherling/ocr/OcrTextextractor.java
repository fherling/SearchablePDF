package de.fherling.ocr;

import com.amazon.textract.pdf.TextLine;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class OcrTextextractor {
    /**
     * Extract text from image using Amazon Textract
     * @param imageBytes
     * @return List of TextLine objects
     */
    public List<TextLine> extractText(ByteBuffer imageBytes){

        try (TextractClient client = TextractClient.builder().build()) {

            DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                    .document(Document.builder()
                            .bytes(SdkBytes.fromByteBuffer(imageBytes))
                            .build())
                    .build();

            DetectDocumentTextResponse result = client.detectDocumentText(request);

            List<TextLine> lines = new ArrayList<>();
            List<Block> blocks = result.blocks();
            BoundingBox boundingBox;
            for (Block block : blocks) {
                if (block.blockType() == BlockType.LINE) {
                    boundingBox = block.geometry().boundingBox();
                    lines.add(new TextLine(boundingBox.left(),
                            boundingBox.top(),
                            boundingBox.width(),
                            boundingBox.height(),
                            block.text()));
                }
            }

            return lines;
        }
    }
}
