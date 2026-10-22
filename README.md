# TruthTrace — Evidence-Aware Document Assistant Prototype

> **Every answer should be open to inspection.**

TruthTrace is an experimental Android document assistant exploring how retrieval strategy and source-linked claims can help users inspect answers based on their documents. It combines document ingestion, multiple retrieval methods, claim labels, and an in-app comparison dashboard. It is a prototype, not a validated research system.

## Exploratory question

How do different retrieval strategies affect the passages selected for document-grounded question answering, and how should an interface communicate when evidence is missing or conflicting?

The application is designed to let a user compare retrieval approaches and inspect whether generated claims appear connected to retrieved source passages. The current demonstration does **not** establish that hybrid retrieval improves citation accuracy or reduces hallucinations in general.

## Main features

1. **Document input**
   - Supports document-focused workflows, including text/PDF and image-based input where supported by the current build.
   - The Android Photo Picker is used for selecting document images.

2. **Retrieval comparison**
   - **Baseline:** generation without retrieved document context; useful as a comparison condition, but not by itself a measured hallucination rate.
   - **BM25 keyword search:** term-based ranking using the Okapi BM25 method.
   - **Lightweight word/character-trigram vector search:** an in-project vector representation with cosine similarity. This is not a pretrained embedding model.
   - **Hybrid RRF:** Reciprocal Rank Fusion (k = 60) combines rankings from BM25 and the lightweight vector method.

3. **Claim labels and source passages**
   - The interface breaks answers into claims and labels them **Supported**, **Contradicted**, **Insufficient Evidence**, or **Unverified**.
   - Citation controls link to retrieved source chunks and their available document metadata.
   - These labels are model-assisted indications for inspection; they are not guarantees that a claim is logically entailed by the cited passage.

4. **Evaluation dashboard**
   - An eight-query demonstration suite compares the four retrieval conditions.
   - The interface presents retrieval indicators such as Recall@k, Precision@k, MRR, and nDCG@k, together with claim/citation, abstention, and latency indicators.
   - **Important limitation:** some claim-faithfulness and citation-support scores are produced using preset formulas rather than independently human-labelled ground truth. Results from this small demonstration should be treated as illustrative development signals, not empirical proof, benchmark-quality evidence, or publication-ready findings.
   - JSON export is provided for inspecting the app’s output; exportability does not by itself make the results scientifically validated.

## Technical stack

- **Platform:** Native Android
- **Language/UI:** Kotlin and Jetpack Compose
- **AI service:** Google Gemini API
- **Retrieval:** Okapi BM25, lightweight word/character-trigram vectors, Reciprocal Rank Fusion
- **Backend / account features:** Firebase components used by the current project
- **Build:** Gradle with Kotlin DSL

Model availability and API access may change. The app requires appropriate configuration and network access for AI-backed features.

## How to explore the demonstration

1. Open the project in Android Studio using a compatible Android SDK.
2. Configure the required Gemini API settings and Firebase configuration for the features you intend to test.
3. Add or select a sample document and try a question.
4. Inspect the retrieved passages and claim labels rather than treating the generated answer as authoritative.
5. Run the in-app demonstration suite to compare the displayed outputs, and export its JSON if you want to examine the results.

## Evaluation cautions

The included test set contains only eight demonstration queries and should not be described as peer-reviewed. Results are sensitive to the documents, queries, retrieval implementation, and AI model configuration. Some displayed claim/citation scores are formula-based and do not represent an independent human audit. A stronger future evaluation would require a documented test corpus, independently annotated relevant passages and claim-support labels, repeatable model/settings records, appropriate baselines, and human review of disagreements.

Until that work is completed, TruthTrace should be presented as a learning prototype that exposes evaluation questions—not as proof that it improves factual accuracy or prevents hallucinations.
