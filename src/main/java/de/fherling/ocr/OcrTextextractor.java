package de.fherling.ocr;

import com.amazon.textract.pdf.TextLine;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;

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

        AmazonTextract client = AmazonTextractClientBuilder.defaultClient();

        DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                .withDocument(new Document()
                        .withBytes(imageBytes));

        DetectDocumentTextResult result = client.detectDocumentText(request);

        List<TextLine> lines = new ArrayList<>();
        List<Block> blocks = result.getBlocks();
        BoundingBox boundingBox;
        for (Block block : blocks) {
            if (block.getBlockType().equals("LINE")) {
                boundingBox = block.getGeometry().getBoundingBox();
                lines.add(new TextLine(boundingBox.getLeft(),
                        boundingBox.getTop(),
                        boundingBox.getWidth(),
                        boundingBox.getHeight(),
                        block.getText()));
            }
        }

        return lines;
    }
}
