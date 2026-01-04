# GitHub Copilot Instructions

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

Repository-Specific Placeholders (fill as needed)
- Languages and versions: TODO
- Code style (formatter/linter): TODO
- Test framework and location: TODO
- Directory structure conventions: TODO
- CI requirements: TODO
- Common utilities/helpers to reuse: TODO

Prompts for Copilot (optional)
- “Implement X following existing patterns, add tests, and avoid new deps.”
- “Refactor Y safely with minimal diff; keep public API unchanged.”
- “Add logging and error handling consistent with current modules.”