# SearchablePDF

A Docker-based OCR service that converts PDF files into searchable PDFs using AWS Textract.

## Features

- Watches directories for new PDF files
- Automatically processes PDFs using AWS Textract OCR
- Outputs searchable PDFs with embedded text layers
- Multi-architecture support (amd64, arm64)
- Runs with non-root user for enhanced security
- **Optimized Docker image** with minimal Alpine Linux base for security and efficiency

## Security

### Non-Root User

The Docker container runs as a non-root user (`ocruser`, UID 1039) for enhanced security. This follows container security best practices by:

- **Preventing privilege escalation**: The application cannot gain root access inside the container
- **Limiting attack surface**: Even if the application is compromised, the attacker has limited permissions
- **Following principle of least privilege**: The user only has permissions needed to run the application

The non-root user has ownership of:
- `/app` - Application JAR and dependencies
- `/ocr-scripts` - Shell scripts for file processing
- `/ocr-input` - Input directory for PDF files
- `/ocr-output` - Output directory for processed PDFs

No root processes run inside the container after startup.

## Docker Usage

### Building the Image

```bash
docker build -f Dockerfile -t searchablepdf:latest .
```

### Running the Container

```bash
docker run -d \
  -v /path/to/input:/ocr-input \
  -v /path/to/output:/ocr-output \
  -e AWS_REGION=eu-central-1 \
  -e AWS_ACCESS_KEY_ID=your_key \
  -e AWS_SECRET_ACCESS_KEY=your_secret \
  searchablepdf:latest
```

### Volume Permissions

When mounting host directories, ensure the non-root user (UID 1039) has read/write permissions:

```bash
# Option 1: Set ownership to match container user
sudo chown -R 1039:1039 /path/to/input /path/to/output

# Option 2: Make directories world-writable (less secure)
chmod 777 /path/to/input /path/to/output
```

## Architecture

### Docker Image Optimization

The Docker image is optimized for minimal size and enhanced security:

- **Multi-stage Build**: Separates build and runtime environments
- **Build Stage**: Uses `maven:3.9-eclipse-temurin-21-alpine` with pre-installed Maven for efficient builds
  - Implements dependency caching for faster rebuilds
  - Only build artifacts are copied to runtime stage
- **Runtime Stage**: Uses minimal `eclipse-temurin:21-jre-alpine` base image
  - Alpine Linux base (~5MB) instead of full distributions (~100MB+)
  - Only essential runtime dependencies: bash, inotify-tools, file
  - No build tools or package managers in final image
  - Built-in health check for container monitoring
- **Security Features**:
  - Runs as non-root user (UID 1039)
  - Minimal attack surface with reduced package footprint
  - No unnecessary development tools in runtime image

### Components

- **File Watcher**: Uses `inotify-tools` to monitor `/ocr-input` for new PDF files
- **AWS Integration**: Leverages AWS SDK for Java to interact with AWS Textract service
- **Shell Scripts**: Bash scripts for file processing and workflow orchestration

## Environment Variables

- `AWS_REGION` - AWS region for Textract service (e.g., `eu-central-1`)
- `AWS_ACCESS_KEY_ID` - AWS access key for authentication
- `AWS_SECRET_ACCESS_KEY` - AWS secret key for authentication

## Development

### Prerequisites

- Java 25 or higher
- Maven 3.6 or higher
- Docker (for containerized deployment)

### Building Locally

```bash
mvn clean package
```

### Running Locally

```bash
./runitLocal.sh
```

## License

See the project repository for license information.
