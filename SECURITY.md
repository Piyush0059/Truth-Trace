# TruthTrace Security & Privacy Policies

## 1. Untrusted Document Data Principle
All document texts extracted from uploaded files or camera scans are treated as untrusted data:
- Content is enclosed within explicit evidence delimiters.
- Document text is isolated from system instructions.
- System prompts instruct models to disregard embedded command overrides.

## 2. API Key Management
- API keys are injected via `BuildConfig.GEMINI_API_KEY` through the Secrets Gradle Plugin.
- No secrets or credentials are hardcoded into source repositories.

## 3. Cloud Access Control & Multi-Tenant Isolation
- Cloud storage enforces zero-trust Firestore Security Rules.
- User data is strictly partitioned by user UID (`/users/{userId}/*`).
- Requests require authenticated user tokens (`request.auth.uid == userId`).
- Unauthenticated requests are rejected at the rule boundary.
