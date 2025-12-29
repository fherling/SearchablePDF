# SearchablePDF

A Docker-based OCR service that converts PDF files into searchable PDFs using AWS Textract.

## Features

- Watches directories for new PDF files
- Automatically processes PDFs using AWS Textract OCR
- Outputs searchable PDFs with embedded text layers
- Multi-architecture support (amd64, arm64)
- Runs with non-root user for enhanced security

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

- **Build Stage**: Uses `eclipse-temurin:21-jdk-alpine` to compile the Java application with Maven
- **Runtime Stage**: Uses smaller `eclipse-temurin:21-jre-alpine` image for running the application
- **File Watcher**: Uses `inotify-tools` to monitor `/ocr-input` for new PDF files
- **AWS Integration**: Leverages AWS SDK for Java to interact with AWS Textract service

## Environment Variables

- `AWS_REGION` - AWS region for Textract service (e.g., `eu-central-1`)
- `AWS_ACCESS_KEY_ID` - AWS access key for authentication
- `AWS_SECRET_ACCESS_KEY` - AWS secret key for authentication

## Development

### Prerequisites

- Java 21 or higher
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
