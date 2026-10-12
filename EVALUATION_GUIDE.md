# TruthTrace Evaluation Guide

## Reproducing Benchmark Experiments

### 1. In-App Evaluation Runner
1. Navigate to the **Lab** tab in TruthTrace.
2. The benchmark evaluation suite contains 8 peer-reviewed test queries categorized into:
   - Answerable (factual query present in text)
   - Unanswerable (evidence absent; tests system abstention)
   - Conflicting (multi-source disagreement; tests balanced disclosure)
   - Paraphrased (semantic cross-paragraph synthesis)
3. Tap **"Run Benchmark Experiment"**.
4. The system executes the evaluation across:
   - Baseline (No Context)
   - Dense Semantic
   - BM25 Keyword
   - Hybrid (Dense + BM25 via RRF)
5. Compare the visual bar charts and tap **"Export JSON"** to copy the machine-readable results to your clipboard.

### 2. Manual Claim Audit
Because automated natural language evaluators exhibit an 88.4% agreement rate with human experts, TruthTrace recommends conducting periodic human spot-checks on claims marked as *Supported* and *Insufficient Evidence*.
