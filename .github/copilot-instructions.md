# SearchablePDF

Docker-based OCR service that converts PDFs into searchable PDFs (image + invisible
text layer) using **AWS Textract**. Java 25 / Maven application, packaged into an Alpine
container that watches a directory and processes PDFs as they arrive.

## Build, test, run

- Build + package (produces `target/searchable-pdf-1.0.jar` + `target/lib/`):
  `mvn clean package`
- Full verify (as CI runs it): `mvn --batch-mode --update-snapshots verify`
- Run a single test: `mvn test -Dtest=OcrPdfFromLocalPdfTest#doOcr`
- Run the app on one file locally: `java -jar target/searchable-pdf-1.0.jar <input.pdf> <output.pdf>`
  (or `./runitLocal.sh <input.pdf> <output.pdf>`, which builds first)
- Build the Docker image: `./build.sh` (runs `mvn clean package` then `docker build`)

Requires Java 25+ (enforced by `maven-enforcer-plugin`) and valid AWS credentials in the
environment (`AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`) for any test/run
that actually calls Textract.

### Tests
- Tests live in `src/test/java` and use JUnit 5 + Mockito.
- Tests that hit AWS Textract or need a rendered PDF are annotated `@Disabled("Only for
  manual testing")` — they are skipped by default and in CI. Do not remove the `@Disabled`
  to make CI exercise live AWS calls; keep AWS-dependent tests opt-in.
- `src/test/resources/ocr-test.pdf` is the manual-test fixture.

## Architecture — the processing pipeline

The runtime flow spans shell scripts and Java; understanding it requires reading several
files together:

1. **`ocr-scripts/watch-files.sh`** (container `ENTRYPOINT`) — runs `inotifywait` on
   `/ocr-input` and, for each new/moved file, calls `ocr-file-aws.sh`.
2. **`ocr-scripts/ocr-file-aws.sh`** — validates MIME type is `application/pdf` (via
   `file --mime-type`), computes an output path under
   `/ocr-output/aws/<file-creation-date>/`, then invokes `java -jar /app/app.jar <in> <out>`.
3. **`OcrRunner`** (`mainClass`, default package) — CLI entry point: validates args and
   input file, delegates to `OcrPdfFromLocalPdf`, maps exceptions to exit codes.
4. **`de.fherling.ocr.OcrPdfFromLocalPdf`** — the orchestrator: loads the PDF with PDFBox,
   renders each page to a 300-DPI JPEG, sends the image bytes to Textract, and assembles the
   searchable output PDF.
5. **`de.fherling.ocr.OcrTextextractor`** — wraps the AWS `TextractClient`; calls
   `detectDocumentText` and maps `BlockType.LINE` blocks into `TextLine` records.
6. **`com.amazon.textract.pdf.PDFDocument`** — builds the output PDF: draws the page image,
   then overlays each `TextLine` as invisible text (`RenderingMode.NEITHER`) positioned from
   Textract's normalized bounding boxes, auto-sizing the font to fit each box.

`process_file.sh` is an unused stub — the real per-file logic is `ocr-file-aws.sh`.

### Package layout
- `de.fherling.ocr` — application logic (this project's own code).
- `com.amazon.textract.pdf` — PDF-building helper classes (`PDFDocument`, `TextLine`,
  `FontInfo`, `ImageType`), derived from AWS Textract sample code. Keep AWS-derived helpers
  in this package.

## Conventions specific to this codebase

- **Java 25 idioms are expected**: `var` for locals, `record` types (`TextLine`, `FontInfo`),
  switch expressions (see `PDFDocument.addPage`), streams over Textract blocks. Match this style.
- **Textract coordinates are normalized** (0–1 fractions of page width/height). Multiply by
  the page/image dimensions when positioning text — see `PDFDocument.calculateFontSize` /
  `addPage`. Don't treat them as absolute points.
- **Output PDF text layer is intentionally invisible** (`RenderingMode.NEITHER`) so the
  original page image stays visible and the text is only for search/copy.
- `OcrRunner` communicates via stdout/stderr + `System.exit` codes (it's a CLI invoked by
  bash), not via thrown exceptions to a caller. Preserve that contract.

## Container & security constraints

- The image runs as **non-root** `ocruser` (UID 1039); only `/app`, `/ocr-scripts`,
  `/ocr-input`, `/ocr-output` are writable by it. Don't add steps that require root at runtime.
- Multi-stage Dockerfile: `maven:3.9-eclipse-temurin-25-alpine` build stage →
  `eclipse-temurin:25-jre-alpine` runtime. Runtime only installs `bash`, `inotify-tools`,
  `file`. Keep the runtime image minimal.
- CI (`.github/workflows/BuildAppAndPublishDockerImage.yml`) builds `linux/amd64` +
  `linux/arm64/v8`, pushes to `ghcr.io`, and signs images with cosign on non-PR events.
- **Security rules for handling untrusted PDFs/images and subprocesses are mandatory** and
  documented in `.github/instructions/security.instructions.md` — read it before changing
  file handling, path construction, subprocess calls, or the Dockerfile. Key rules to apply
  even without reading that file: (1) resolve/normalize all file paths and confirm they stay
  inside the dedicated input/output directories — reject absolute paths or `..` segments;
  (2) never invoke subprocesses with `shell=True` or concatenated command strings — pass
  arguments as arrays and set timeouts; (3) treat every uploaded PDF/image as malicious —
  enforce content-type, size, and dimension limits, and disable active content (PDF
  JavaScript, `/AA` actions, embedded files); (4) never hardcode secrets, and redact
  secrets/PII from logs; (5) fail closed — reject input when validation is uncertain.
