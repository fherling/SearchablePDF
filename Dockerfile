# Build stage - compile and package the application
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy Maven files first for better layer caching
COPY pom.xml .
COPY src ./src

# Build the application and copy dependencies
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    mvn dependency:copy-dependencies -DoutputDirectory=target/lib

# Runtime stage - smaller JRE image
FROM eclipse-temurin:21-jre-alpine

# Install runtime dependencies
RUN apk add --no-cache \
    aws-cli \
    bash \
    inotify-tools \
    file

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

ENTRYPOINT ["/ocr-scripts/watch-files.sh"]