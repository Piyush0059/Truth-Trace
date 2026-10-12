# TruthTrace — AI Research Document Assistant

> **"Every Answer. Traceable Evidence."**  
> *AI Answers You Can Investigate.*

TruthTrace is an evidence-first, research-grade Android document assistant that synthesizes verifiable, evidence-grounded answers, attributes every factual claim to exact document passages, detects unsupported assertions, and provides a reproducible evaluation laboratory to measure how retrieval quality impacts factual faithfulness.

---

## Core Research Question
> **"How does retrieval strategy affect factual faithfulness and citation correctness in document-grounded question answering?"**

Standard RAG implementations frequently assume that high vector cosine similarity equates to factual truth. TruthTrace separates topical alignment from logical entailment, demonstrating that combining dense semantic retrieval with lexical BM25 matching (Hybrid Reciprocal Rank Fusion) provides superior citation precision and decreases hallucination.

---

## Key Features & Capabilities

1. **Interactive 3D Evidence Constellation**
   - High-performance perspective-projected 3D scene built in Jetpack Compose Canvas.
   - Interactive touch-drag rotation, atmospheric particles, floating document planes, and dynamic retrieval beam animations.
   - Real-time application state synchronization (Idle, Retrieving, Verifying, Complete).

2. **Multimodal Document Scanner (Gemini 3.1 Pro)**
   - Android zero-permission Photo Picker (`PickVisualMedia`) for document photo upload.
   - High-resolution multimodal OCR and semantic understanding via `gemini-3.1-pro-preview`.

3. **High Thinking Mode (`ThinkingLevel.HIGH`)**
   - Integrates Gemini 3.1 Pro with High Thinking mode for deep claim-to-passage natural language inference (NLI).

4. **Multi-Strategy Retrieval Engine**
   - **Baseline:** Parametric generation without retrieved context (quantifies baseline hallucination).
   - **Dense Semantic:** Subword n-gram vector embeddings and cosine similarity.
   - **BM25 Keyword:** Okapi BM25 ranking function with term frequency and document length normalization.
   - **Hybrid RRF:** Reciprocal Rank Fusion ($k = 60$) blending lexical and semantic signals.

5. **Atomic Claim Verification & Citation Integrity**
   - Answers decomposed into atomic claims.
   - Conservative 4-state classification: **Supported** (Teal), **Contradicted** (Coral), **Insufficient Evidence** (Amber), **Unverified** (Gray).
   - Verifiable citation pills `[S1]`, `[S2]` linking to source chunk text, document name, page number, and chunk ID.

6. **Reproducible Research Lab & Evaluation Dashboard**
   - Controlled benchmark comparison across all 4 strategies.
   - Computes empirical metrics: **Recall@k**, **Precision@k**, **MRR**, **nDCG@k**, **Claim-Level Faithfulness**, **Citation Support Accuracy**, **Abstention F1**, and **Latency (ms)**.
   - JSON export for sharing and scientific publication.

7. **Zero-Trust Security & Firebase Integration**
   - Firestore Enterprise database scoped per-user (`users/{userId}/...`).
   - Google Sign-In via Jetpack Credential Manager (`GetSignInWithGoogleOption`).
   - Untrusted document data sanitization and prompt injection defense.

---

## Technical Stack
- **Platform:** Native Android (Min API 24, Target API 36)
- **Language:** Kotlin 2.2.10
- **UI Framework:** Jetpack Compose (Material Design 3)
- **AI Models:** `gemini-3.1-pro-preview` (High Thinking & Multimodal), `gemini-3.5-flash`, `gemini-3.1-flash-lite-preview`
- **Cloud Backend:** Firebase Firestore (Custom Database ID), Firebase Auth, Credential Manager
- **Retrieval:** Okapi BM25, Dense Semantic Vector Search, Reciprocal Rank Fusion
- **Build System:** Gradle (Kotlin DSL), Secrets Gradle Plugin

---

## Setup & Configuration

1. **Gemini API Key:**
   Configure your Gemini API key in the Secrets panel in AI Studio (or `.env` as `GEMINI_API_KEY=...`).
2. **Google Sign-In & Firebase:**
   Firebase Firestore and Auth are pre-configured with SHA-1/SHA-256 certificate fingerprints and the generated Web Client ID.
3. **Running the App:**
   Open the app in the Streaming Android Emulator or build with Gradle.
