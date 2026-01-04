---
applyTo: **
---
Security best practices for Copilot in this repository (SearchablePDF)

Purpose
- Guide Copilot to produce secure code and configurations when handling user-supplied PDFs/images and running OCR/conversion pipelines.

General
- Apply least privilege everywhere: run as non-root, restrict file permissions, and isolate processing to a sandbox/work directory.
- Never use eval/exec or dynamic code loading with untrusted input.
- Prefer safe libraries over rolling custom crypto or parsers.

Untrusted file handling (PDFs, images)
- Treat all uploaded files as malicious. Enforce:
  - Content-type and file extension checks.
  - Max file size and page/image dimensions.
  - Memory/time limits during decoding and OCR.
- Disable active content: do not execute PDF JavaScript, annotations with actions (/AA), embedded files, or external references.
- Use safe parsers: avoid using insecure deserialization (e.g., pickle). Prefer JSON or safe YAML loaders.
- Strip and normalize metadata; never trust EXIF or PDF metadata; avoid writing user-controlled metadata into outputs.
- Reject archives or container formats; if accepting them, detect and block zip-bombs via recursion, count, and size limits.

Paths and file I/O
- Prevent path traversal:
  - Resolve and validate paths stay within a dedicated base directory.
  - Do not accept absolute paths or “..” segments from users.
- Use atomic writes and unique temp files. Prefer OS temp APIs; set restrictive permissions (umask).
- Do not write to shared or system directories. Avoid 0777 permissions.

Subprocess and external tools (e.g., Tesseract/Ghostscript)
- Do not use shell=True. Pass args as arrays. Validate/escape every argument.
- Set timeouts and check return codes. Capture stdout/stderr safely. Limit environment exposure.
- Constrain resource usage (ulimits, cgroups, job timeouts). Fail fast on excessive CPU/memory.

Networking
- Default to no outbound network calls during file processing.
- If network is required, use HTTPS with certificate validation. No HTTP. No custom TLS unless using proven libraries.
- Do not embed remote URLs into output PDFs.

Secrets and configuration
- Never hardcode secrets, tokens, or API keys. Load from environment or a secret store.
- Do not log secrets or PII. Redact sensitive data in logs. Keep logs minimal.
- Use separate configs for dev/test/prod. Disable debug modes in production.

Dependency hygiene
- Pin versions and use lock files. Prefer checksum/hash verification where supported.
- Keep dependencies updated. Remove unused packages. Avoid abandoned or untrusted libraries.
- Run SCA (software composition analysis) and fix high/critical vulnerabilities promptly.

Web/API surfaces (if applicable)
- Validate and sanitize all inputs (size, type, format). Enforce strict schemas.
- Limit upload size; stream uploads; reject oversized or malformed content early.
- Return generic error messages; do not leak stack traces. Implement rate limiting.
- Apply secure headers (CORS, CSP where relevant), and HTTPS-only.

Deserialization and serialization
- Use safe JSON/YAML loaders (no object hooks). Avoid pickle or unsafe formats.
- Validate all structured inputs against a schema before use.

Testing and scanning
- Add unit/integration tests for security boundaries (path resolution, file limits, malicious PDFs/images).
- Enable GitHub Advanced Security features:
  - Code scanning (CodeQL) with language-appropriate queries.
  - Secret scanning and push protection.
  - Dependabot alerts and updates.
- Use linters and security tools:
  - Python: bandit, ruff with security rules.
  - JavaScript/TypeScript: eslint security plugins, npm audit.
  - Shell: shellcheck; avoid complex shells entirely.

Error handling and observability
- Fail closed: if validation or parsing is uncertain, reject the input.
- Implement structured logging with correlation IDs; do not include file contents or sensitive fields.
- Monitor for abnormal resource usage and processing times.

Output PDF safety
- Do not include active content or executable actions. Flatten content when possible.
- Embed only the necessary text layer; avoid embedding user-controlled scripts or attachments.
- Sanitize fonts and images; avoid external references.

Copilot-specific guidance
- Prefer library APIs over shell commands. If must call subprocess, use secure patterns described.
- Suggest environment-based config and explicit validation helpers.
- Default to secure timeouts, limits, and non-root execution.
- Avoid generating code that:
  - Uses shell=True or concatenated command strings.
  - Writes to arbitrary paths from user input.
  - Deserializes untrusted data with unsafe loaders.
  - Hardcodes secrets or disables verification.
- Provide secure examples with validation, resource limits, error handling, and logging redaction.

Minimal secure patterns (language-agnostic)
- Input validation: check type, size, and format before processing.
- Path safety: resolve, normalize, and ensure inside a sandbox directory.
- Subprocess: argv arrays, no shell, timeouts, check return codes.
- Resource limits: enforce CPU, memory, and file size caps.
- Logging: structured, redacted, no PII/secrets.

Approval and exceptions
- Security exceptions require review and documentation (risk, justification, compensating controls).
- Revisit exceptions periodically and remove when possible.

By default, generate code that rejects unsafe inputs, runs with strict limits, and avoids active/remote content. When uncertain, ask for clarification and choose the safer option.