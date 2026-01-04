# Build stage - compile and package the application
# Using maven image which includes Maven pre-installed, reducing build time and image layers
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy Maven files first for better layer caching
COPY pom.xml .

# Download dependencies first (cached layer if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application and copy dependencies
RUN mvn clean package -DskipTests && \
    mvn dependency:copy-dependencies -DoutputDirectory=target/lib

# Runtime stage - smaller JRE image with Alpine Linux for minimal footprint
FROM eclipse-temurin:21-jre-alpine

# Add image metadata
LABEL org.opencontainers.image.title="SearchablePDF" \
      org.opencontainers.image.description="Docker-based OCR service that converts PDFs into searchable PDFs using AWS Textract" \
      org.opencontainers.image.vendor="fherling" \
      org.opencontainers.image.source="https://github.com/fherling/SearchablePDF"

# Install minimal runtime dependencies
# bash: Required for shell scripts with bash-specific features
# inotify-tools: Required for inotifywait file system monitoring
# file: Required for MIME type detection
RUN apk add --no-cache \
    bash \
    inotify-tools \
    file && \
    rm -rf /var/cache/apk/*

# Create directories
RUN mkdir -p /ocr-input /ocr-output /app/lib

# Copy dependencies first (changes less often = better caching)
COPY --from=builder /build/target/lib /app/lib

# Copy application jar (changes more frequently)
COPY --from=builder /build/target/searchable-pdf-1.0.jar /app/app.jar

# Copy scripts
COPY ./ocr-scripts /ocr-scripts

# Create non-root user
RUN addgroup -g 65538 ocrgroup && \
    adduser -u 1039 -G ocrgroup -D -s /bin/bash ocruser && \
    chown -R ocruser:ocrgroup /ocr-scripts /ocr-input /ocr-output /app

USER ocruser
WORKDIR /ocr-scripts

# Health check to ensure the application is running and directories are accessible
# Checks both that inotifywait is running and that required directories exist
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD pgrep -f "inotifywait" > /dev/null && test -d /ocr-input && test -d /ocr-output || exit 1

ENTRYPOINT ["/ocr-scripts/watch-files.sh"]