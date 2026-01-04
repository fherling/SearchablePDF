# SearchablePDF

Create searchable PDFs by adding an OCR text layer on top of an existing PDF.

How it works (high level)

- Renders each page of the input PDF to an image (PDFBox)
- Sends images to AWS Textract (AWS SDK v2)
- Writes a new PDF with the original page images plus an invisible text overlay (searchable text layer)

Requirements

- Java 21+ (CI uses Temurin 21; Maven Enforcer requires Java >= 21)
- Maven 3.9+
- AWS credentials configured for Textract (via the AWS SDK v2 default credential chain)

Build

```bash
mvn clean package
```

Test

```bash
mvn clean test
```

CI parity:

```bash
mvn --batch-mode --update-snapshots verify
```

Run locally

```bash
java -jar target/searchable-pdf-1.0.jar <input.pdf> <output.pdf>
```

Docker watcher
Build the image:

```bash
docker build -f Dockerfile -t searchablepdf-local:test .
```

Run the watcher (process PDFs dropped into `./ocr-input`):

```bash
docker run --rm \
  -v "$PWD/ocr-input:/ocr-input" \
  -v "$PWD/ocr-output:/ocr-output" \
  -e AWS_REGION=eu-central-1 \
  searchablepdf-local:test
```

Notes

- The scripts under `ocr-scripts/` are intended for Linux containers (they use `inotifywait` and GNU-style `stat`/`date`).
- On macOS, run the JAR directly instead of the watcher scripts.
