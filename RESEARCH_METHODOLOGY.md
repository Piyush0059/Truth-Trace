# TruthTrace Research Methodology

## 1. Research Question
**"How does retrieval strategy affect factual faithfulness and citation correctness in document-grounded question answering?"**

## 2. Experimental Configurations
Four configurations are evaluated against identical document collections and queries:
1. **Baseline (Zero Retrieval Context):** Evaluates parametric knowledge hallucination in generative models without external documents.
2. **Dense Semantic Retrieval:** Measures vector similarity coverage on semantic paraphrases.
3. **BM25 Lexical Retrieval:** Evaluates exact-match precision on technical constants, acronyms, and formulas.
4. **Hybrid Retrieval (RRF):** Evaluates multi-channel consensus.

## 3. Metrics Formulation
- **Recall@k:** $\frac{|\text{Retrieved}_k \cap \text{Relevant}|}{|\text{Relevant}|}$
- **Precision@k:** $\frac{|\text{Retrieved}_k \cap \text{Relevant}|}{k}$
- **Mean Reciprocal Rank (MRR):** $\frac{1}{|Q|} \sum_{i=1}^{|Q|} \frac{1}{\text{rank}_i}$
- **nDCG@k:** $\frac{\text{DCG}_k}{\text{IDCG}_k}$ where $\text{DCG}_k = \sum_{i=1}^k \frac{2^{\text{rel}_i} - 1}{\log_2(i + 1)}$
- **Claim Faithfulness:** $\frac{|\text{Supported Claims}|}{|\text{Total Extracted Claims}|}$
- **Citation Correctness:** Proportion of citations referencing verified entailing passages.
- **Abstention F1:** Performance identifying unanswerable queries.
