package de.fherling.ocr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazon.textract.pdf.TextLine;

import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.TextractClientBuilder;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.BoundingBox;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Geometry;

@ExtendWith(MockitoExtension.class)
class OcrTextextractorTest {

    @Mock
    private TextractClient mockClient;

    @Mock
    private TextractClientBuilder mockBuilder;

    private OcrTextextractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new OcrTextextractor();
    }

    @Test
    void extractText_shouldReturnTextLinesFromLineBlocks() {
        // Arrange
        ByteBuffer imageBytes = ByteBuffer.wrap(new byte[]{1, 2, 3});
        
        Block lineBlock1 = createBlock(BlockType.LINE, "First line", 0.1f, 0.2f, 0.5f, 0.05f);
        Block lineBlock2 = createBlock(BlockType.LINE, "Second line", 0.1f, 0.3f, 0.6f, 0.05f);
        
        DetectDocumentTextResponse response = DetectDocumentTextResponse.builder()
                .blocks(lineBlock1, lineBlock2)
                .build();

        try (MockedStatic<TextractClient> mockedStatic = mockStatic(TextractClient.class)) {
            when(mockBuilder.build()).thenReturn(mockClient);
            mockedStatic.when(TextractClient::builder).thenReturn(mockBuilder);
            when(mockClient.detectDocumentText(any(DetectDocumentTextRequest.class)))
                    .thenReturn(response);

            // Act
            List<TextLine> result = extractor.extractText(imageBytes);

            // Assert
            assertEquals(2, result.size());
            
            TextLine first = result.get(0);
            assertEquals(0.1f, first.left());
            assertEquals(0.2f, first.top());
            assertEquals(0.5f, first.width());
            assertEquals(0.05f, first.height());
            assertEquals("First line", first.text());
            
            TextLine second = result.get(1);
            assertEquals(0.1f, second.left());
            assertEquals(0.3f, second.top());
            assertEquals(0.6f, second.width());
            assertEquals(0.05f, second.height());
            assertEquals("Second line", second.text());
        }
    }

    @Test
    void extractText_shouldFilterOutNonLineBlocks() {
        // Arrange
        ByteBuffer imageBytes = ByteBuffer.wrap(new byte[]{1, 2, 3});
        
        Block lineBlock = createBlock(BlockType.LINE, "Line text", 0.1f, 0.2f, 0.5f, 0.05f);
        Block pageBlock = createBlock(BlockType.PAGE, "Page", 0.0f, 0.0f, 1.0f, 1.0f);
        Block wordBlock = createBlock(BlockType.WORD, "Word", 0.1f, 0.2f, 0.1f, 0.05f);
        
        DetectDocumentTextResponse response = DetectDocumentTextResponse.builder()
                .blocks(pageBlock, lineBlock, wordBlock)
                .build();

        try (MockedStatic<TextractClient> mockedStatic = mockStatic(TextractClient.class)) {
            when(mockBuilder.build()).thenReturn(mockClient);
            mockedStatic.when(TextractClient::builder).thenReturn(mockBuilder);
            when(mockClient.detectDocumentText(any(DetectDocumentTextRequest.class)))
                    .thenReturn(response);

            // Act
            List<TextLine> result = extractor.extractText(imageBytes);

            // Assert
            assertEquals(1, result.size());
            assertEquals("Line text", result.get(0).text());
        }
    }

    @Test
    void extractText_shouldReturnEmptyListWhenNoLineBlocks() {
        // Arrange
        ByteBuffer imageBytes = ByteBuffer.wrap(new byte[]{1, 2, 3});
        
        Block pageBlock = createBlock(BlockType.PAGE, "Page", 0.0f, 0.0f, 1.0f, 1.0f);
        
        DetectDocumentTextResponse response = DetectDocumentTextResponse.builder()
                .blocks(pageBlock)
                .build();

        try (MockedStatic<TextractClient> mockedStatic = mockStatic(TextractClient.class)) {
            when(mockBuilder.build()).thenReturn(mockClient);
            mockedStatic.when(TextractClient::builder).thenReturn(mockBuilder);
            when(mockClient.detectDocumentText(any(DetectDocumentTextRequest.class)))
                    .thenReturn(response);

            // Act
            List<TextLine> result = extractor.extractText(imageBytes);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void extractText_shouldHandleEmptyResponse() {
        // Arrange
        ByteBuffer imageBytes = ByteBuffer.wrap(new byte[]{1, 2, 3});
        
        DetectDocumentTextResponse response = DetectDocumentTextResponse.builder()
                .blocks(List.of())
                .build();

        try (MockedStatic<TextractClient> mockedStatic = mockStatic(TextractClient.class)) {
            when(mockBuilder.build()).thenReturn(mockClient);
            mockedStatic.when(TextractClient::builder).thenReturn(mockBuilder);
            when(mockClient.detectDocumentText(any(DetectDocumentTextRequest.class)))
                    .thenReturn(response);

            // Act
            List<TextLine> result = extractor.extractText(imageBytes);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void extractText_shouldProperlyConvertBoundingBoxCoordinates() {
        // Arrange
        ByteBuffer imageBytes = ByteBuffer.wrap(new byte[]{1, 2, 3});
        
        Block lineBlock = createBlock(BlockType.LINE, "Test", 0.123f, 0.456f, 0.789f, 0.012f);
        
        DetectDocumentTextResponse response = DetectDocumentTextResponse.builder()
                .blocks(lineBlock)
                .build();

        try (MockedStatic<TextractClient> mockedStatic = mockStatic(TextractClient.class)) {
            when(mockBuilder.build()).thenReturn(mockClient);
            mockedStatic.when(TextractClient::builder).thenReturn(mockBuilder);
            when(mockClient.detectDocumentText(any(DetectDocumentTextRequest.class)))
                    .thenReturn(response);

            // Act
            List<TextLine> result = extractor.extractText(imageBytes);

            // Assert
            assertEquals(1, result.size());
            TextLine textLine = result.get(0);
            assertEquals(0.123f, textLine.left(), 0.0001f);
            assertEquals(0.456f, textLine.top(), 0.0001f);
            assertEquals(0.789f, textLine.width(), 0.0001f);
            assertEquals(0.012f, textLine.height(), 0.0001f);
            assertEquals("Test", textLine.text());
        }
    }

    @Test
    void extractText_shouldCloseClientAfterUse() {
        // Arrange
        ByteBuffer imageBytes = ByteBuffer.wrap(new byte[]{1, 2, 3});
        
        DetectDocumentTextResponse response = DetectDocumentTextResponse.builder()
                .blocks(List.of())
                .build();

        try (MockedStatic<TextractClient> mockedStatic = mockStatic(TextractClient.class)) {
            when(mockBuilder.build()).thenReturn(mockClient);
            mockedStatic.when(TextractClient::builder).thenReturn(mockBuilder);
            when(mockClient.detectDocumentText(any(DetectDocumentTextRequest.class)))
                    .thenReturn(response);

            // Act
            extractor.extractText(imageBytes);

            // Assert
            verify(mockClient).close();
        }
    }

    private Block createBlock(BlockType type, String text, float left, float top, float width, float height) {
        return Block.builder()
                .blockType(type)
                .text(text)
                .geometry(Geometry.builder()
                        .boundingBox(BoundingBox.builder()
                                .left(left)
                                .top(top)
                                .width(width)
                                .height(height)
                                .build())
                        .build())
                .build();
    }
}