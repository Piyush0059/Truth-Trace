# TruthTrace Architecture Specification

## 1. System Overview
TruthTrace integrates a client-side document processing and retrieval pipeline with cloud-based Gemini reasoning and Firestore persistence:

```
[Document Ingestion (PDF/TXT/Photo)]
             │
             ▼
[DocumentChunker] (Chunking: 450 chars, Overlap: 80 chars, Page/Offset Metadata)
             │
             ├──► [BM25Retriever] (Okapi BM25, Robertson-Spärck Jones IDF)
             │
             ├──► [DenseRetriever] (Subword n-gram embedding, Cosine Sim)
             │
             └──► [HybridRetriever] (Reciprocal Rank Fusion k=60)
                         │
                         ▼
             [Candidate Passages Ranked]
                         │
                         ▼
             [Gemini 3.1 Pro (ThinkingLevel.HIGH)]
                         │
        ┌────────────────┴────────────────┐
        ▼                                 ▼
[Evidence-Grounded Answer]      [Atomic Claims Decomposed]
[With [S1], [S2] Citations]     [Supported / Contradicted / Insufficient]
        │                                 │
        └────────────────┬────────────────┘
                         ▼
             [Evidence Inspector & Constellation 3D]
                         │
                         ▼
             [Firestore Per-User Sync]
```

## 2. Ingestion & Chunking
- Documents are partitioned into sliding windows snapped to sentence boundaries (`.`, `\n`, `!`, `?`).
- Chunks retain immutable `documentId`, `documentTitle`, `pageNumber`, `chunkIndex`, `startCharOffset`, and `endCharOffset`.

## 3. Retrieval Models
- **BM25 Lexical:** Uses inverted document frequencies over non-stopword tokens, prioritizing exact terminology and numerical constants.
- **Dense Semantic:** Encodes positional word and subword character trigrams into a normalized vector space, accommodating synonyms and paraphrasing.
- **Hybrid RRF:** Merges rankings using $RRF(d) = \sum_{m} \frac{1}{60 + \text{rank}_m(d)}$, producing robust rankings across both technical jargon and broad conceptual queries.

## 4. Verification Logic
- **Natural Language Inference:** Distinguishes premise entailment from topical overlap.
- **Conservative Abstention:** If a claim cannot be confirmed from the retrieved chunks, it is marked as `INSUFFICIENT` rather than assumed true or false.
