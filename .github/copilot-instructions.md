# GitHub Copilot Instructions

Repository summary
- This is a Java CLI + Dockerized watcher that converts PDFs into searchable PDFs by:
	- Rendering each page to an image (PDFBox)
	- Sending images to AWS Textract (AWS SDK v2)
	- Overlaying extracted text back onto the PDF (searchable text layer)

Tech / runtime
- Project type: Maven Java application producing an executable JAR + Docker image
- Language/runtime: Java 21 (CI uses Temurin 21; Maven Enforcer requires Java >= 21)
- Key libraries: AWS SDK v2 (Textract, S3), Apache PDFBox 3.x, JUnit 5 + Mockito

Purpose
- Align Copilot’s suggestions with this repository’s expectations.
- Favor small, high-quality, and maintainable changes.

General Guidelines
- Match existing patterns, naming, and file organization.
- Keep diffs minimal and focused. Avoid broad refactors unless requested.
- Do not add new dependencies unless explicitly asked.
- Write defensive code: validate inputs, handle errors, and avoid side effects.
- Avoid secrets and hardcoded credentials. Use environment variables or configuration files.
- Preserve existing licenses/headers and module docstrings.

Testing
- When adding or changing code, include minimal, focused tests.
- Prefer deterministic tests and avoid external network calls.
- Mock I/O and external services where possible.

Documentation
- Add clear docstrings for public functions/classes.
- Inline comments for non-obvious logic or constraints.
- Update README or usage docs when altering behavior or interfaces.

Error Handling and Logging
- Fail fast on invalid input with clear messages.
- Use existing logging patterns; avoid noisy logs and PII in messages.

Performance and Security
- Prefer simple, readable solutions over premature optimization.
- Sanitize user input and validate external data.
- Avoid insecure defaults; use safe libraries and APIs.

Project layout (start here)
- Build definition: `pom.xml`
- App entrypoint: `src/main/java/OcrRunner.java` (expects `<input.pdf> <output.pdf>`)
- OCR pipeline:
	- `src/main/java/de/fherling/ocr/OcrPdfFromLocalPdf.java` (renders pages, calls Textract, writes output)
	- `src/main/java/de/fherling/ocr/OcrTextextractor.java` (AWS Textract client + LINE block mapping)
	- `src/main/java/com/amazon/textract/pdf/PDFDocument.java` (PDF creation + text overlay)
- Tests:
	- `src/test/java/**` (JUnit 5)
	- `OcrPdfFromLocalPdfTest` is disabled and intended for manual runs
- Container runtime automation:
	- `Dockerfile` builds a non-root image; entrypoint is `ocr-scripts/watch-files.sh`
	- `ocr-scripts/ocr-file-aws.sh` processes files dropped into `/ocr-input` and writes to `/ocr-output/...`
- Generated build output: `target/` (do not edit; ignore when making changes)

Build / test / run (validated)
- Prereqs (local): Java 21+ and Maven 3.9+
	- CI uses Java 21; local Java newer than 21 is OK, but Mockito may emit agent-loading warnings.
- Always use Maven from repo root.

Bootstrap
- No separate bootstrap step; Maven will download dependencies on first build.

Build (local)
- `mvn clean package`

Test (local)
- `mvn clean test`
- CI parity (recommended before PR): `mvn --batch-mode --update-snapshots verify`
	- This command was run successfully on macOS in this repo.

Run (local)
- `java -jar target/searchable-pdf-1.0.jar <input.pdf> <output.pdf>`
	- Note: the actual OCR call uses AWS Textract at runtime.
	- Provide AWS credentials via the standard AWS SDK v2 default credential chain (e.g., `AWS_PROFILE`, env vars, or mounted `~/.aws`).

Docker (validated build)
- Build: `docker build -f Dockerfile -t searchablepdf-local:test .`
- Run watcher (example):
	- `docker run --rm -v "$PWD/ocr-input:/ocr-input" -v "$PWD/ocr-output:/ocr-output" -e AWS_REGION=eu-central-1 searchablepdf-local:test`
	- Provide credentials using your preferred method (e.g., `-e AWS_PROFILE=...` plus mounting your AWS config/credentials, or `AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY`).

Platform gotchas
- The watcher scripts under `ocr-scripts/` are designed for Linux containers:
	- They use `inotifywait` and GNU-style `stat`/`date` flags that do not work on macOS.
	- For local (macOS) execution, run the JAR directly instead of the watcher scripts.

CI / pre-checkin expectations
- GitHub Actions workflow: `.github/workflows/BuildAppAndPublishDockerImage.yml`
	- Runs `mvn ... verify` on Java 21
	- Builds a multi-arch Docker image (push/sign only outside PRs)
- Dependabot config: `.github/dependabot.yml`

Agent workflow guidance (to minimize exploration)
- Prefer editing the OCR pipeline in `de/fherling/ocr/` and PDF overlay logic in `com/amazon/textract/pdf/`.
- Add/adjust unit tests in `src/test/java/`.
- Avoid network calls in tests; mock AWS clients (see existing Mockito patterns).
- Trust this file as the canonical build/run recipe; only search the repo when these instructions are insufficient or out of date.

Prompts for Copilot (optional)
- “Implement X following existing patterns, add tests, and avoid new deps.”
- “Refactor Y safely with minimal diff; keep public API unchanged.”
- “Add logging and error handling consistent with current modules.”