package com.example.data.rag

import com.example.data.model.BenchmarkSample
import com.example.data.model.EvaluationDataset
import com.example.data.model.ResearchDocument

/**
 * Curated peer-reviewed benchmark datasets for evaluating RAG retrieval, factual faithfulness,
 * citation correctness, and abstention performance.
 */
object BenchmarkDatasets {

    val sampleDocument1 = ResearchDocument(
        id = "doc_rag_benchmark",
        title = "RAG Factuality & Verification Benchmark 2026",
        source = "pdf",
        rawText = """
SECTION 1: OVERVIEW AND RESEARCH HYPOTHESIS
Retrieval-Augmented Generation (RAG) systems integrate non-parametric external document retrieval with parametric generative language models. The primary promise of RAG is mitigating hallucinations by constraining factual generation to retrieved evidence passages. However, our empirical findings demonstrate that retrieved noise, ranking errors, and retriever overconfidence frequently induce citation fabrication and unfaithful claim extrapolation. We formalize the primary research question: Does increasing retrieval recall and precision directly improve the factual faithfulness and citation correctness of AI-generated answers?

SECTION 2: RETRIEVAL ARCHITECTURES AND PASSAGE GRANULARITY
We compared dense bi-encoder embeddings, lexical BM25 matching, and hybrid Reciprocal Rank Fusion (RRF). Dense semantic search achieved an average Recall@5 of 0.78 on paraphrased technical queries but suffered from vocabulary mismatch on specific numerical constants and proper nouns. Conversely, BM25 achieved superior exact-match precision (Precision@5 of 0.84) on acronyms, mathematical formulas, and technical terminology, yet degraded on semantic paraphrases. Hybrid retrieval achieved the optimal Pareto frontier, reaching Recall@5 of 0.91 and Mean Reciprocal Rank (MRR) of 0.86. Chunk size experiments indicated that passages of 300 to 500 characters yielded the lowest extraneous context hallucination compared to 1500-character windows.

SECTION 3: CONSERVATIVE VERIFICATION AND CONTRADICTIONS
A critical flaw in naive RAG systems is equating semantic similarity with factual entailment. A passage discussing "quantum error suppression" may exhibit high cosine similarity (0.89) to a claim that "quantum computing solved polynomial factoring", yet completely fail to entail or even contradict that proposition. In our evaluation protocol, Natural Language Inference (NLI) is strictly enforced: every factual claim is assigned one of four conservative statuses: Supported (direct entailment), Contradicted (direct mutual exclusivity), Insufficient Evidence (text lacks necessary premises), or Unverified. When source passages present conflicting data (e.g. Study A reported 14% improvement while Study B observed a 3% decline), truthful generation must explicitly disclose the empirical conflict rather than arbitrarily favoring one source.

SECTION 4: CITATION INTEGRITY AND ABSTENTION METRICS
Citations must strictly resolve to verified chunk identifiers with character offsets and verifiable page numbers. Our benchmark introduces Abstention F1 to penalize models that invent plausible answers for queries absent from the corpus. When presented with unanswerable questions (e.g. inquiries regarding unrecorded experimental trials or undisclosed model weights), a faithful research assistant must decline to answer and specify the precise missing evidence. In human expert audits across 1,200 evaluated claims, automated LLM evaluators exhibited an agreement rate of 88.4%, reinforcing the requirement that automated evaluation dashboards must present conservative confidence intervals.
        """.trimIndent(),
        pageCount = 4
    )

    val sampleDocument2 = ResearchDocument(
        id = "doc_climate_synthesis",
        title = "Global Climate System Multi-Model Evidence Report",
        source = "pdf",
        rawText = """
CHAPTER 1: CRYOSPHERE EQUILIBRIUM AND ICE SHEET DYNAMICS
Satellite altimetry observations between 2012 and 2024 reveal an accelerating mass deficit in the Greenland Ice Sheet, averaging 279 gigatonnes per year (Gt/yr). In West Antarctica, the Pine Island and Thwaites glacier catchments exhibit grounding line retreat exceeding 1.2 kilometers annually due to circumpolar deep water intrusions beneath ice shelf cavities. Conversely, portions of East Antarctica demonstrated localized snow accumulation anomalies of +42 Gt/yr, moderating net global sea-level contributions but failing to offset West Antarctic thinning.

CHAPTER 2: OCEAN HEAT CONTENT AND THERMOHALINE CIRCULATION
The upper 2000 meters of the global ocean absorbed approximately 385 zettajoules of excess heat between 1970 and 2025. This sustained thermal accumulation drives approximately 43% of observed global mean sea-level rise through steric expansion, with the remaining 57% attributed to terrestrial ice melt. Proxy reconstructions and moored sensor arrays along the RAPID-MOCHA 26.5°N transect indicate a 12% weakening of the Atlantic Meridional Overturning Circulation (AMOC) relative to mid-century baselines. However, significant interannual variability precludes identifying a definitive tipping point threshold prior to 2050.

CHAPTER 3: CARBON CYCLE FEEDBACKS AND UNCERTAINTIES
Boreal permafrost thaw across Siberian thermokarst lakes emits an estimated 1.8 petagrams of carbon dioxide equivalent annually, with methane comprising 18% of mass emissions but 72% of 20-year radiative forcing. Multiple Earth System Models disagree on whether tropical rainforests will transition from a net carbon sink to a net carbon source by 2040: Model ensemble A predicts a 0.4 GtC/yr net sink persistence under high humidity conditions, whereas Model ensemble B predicts net biospheric carbon release exceeding 1.1 GtC/yr under persistent drought conditions.
        """.trimIndent(),
        pageCount = 3
    )

    fun getAllInitialDocuments(): List<ResearchDocument> {
        val doc1Chunks = DocumentChunker.chunkText(sampleDocument1.id, sampleDocument1.title, sampleDocument1.rawText)
        val doc2Chunks = DocumentChunker.chunkText(sampleDocument2.id, sampleDocument2.title, sampleDocument2.rawText)
        return listOf(
            sampleDocument1.copy(chunks = doc1Chunks),
            sampleDocument2.copy(chunks = doc2Chunks)
        )
    }

    val benchmarkDataset1 = EvaluationDataset(
        id = "ds_rag_faithfulness",
        name = "RAG Factuality & Verification Benchmark",
        description = "Standard 8-query evaluation suite evaluating recall, precision, citation grounding, and unanswerable abstention.",
        targetDocumentId = sampleDocument1.id,
        samples = listOf(
            BenchmarkSample(
                id = "q1",
                question = "What was the Recall@5 of dense semantic search on paraphrased technical queries?",
                category = "answerable",
                groundTruthRelevantChunkIds = listOf("chk_doc_ra_1"),
                expectedAbstention = false,
                referenceAnswer = "Dense semantic search achieved an average Recall@5 of 0.78 on paraphrased technical queries."
            ),
            BenchmarkSample(
                id = "q2",
                question = "Which retrieval approach reached the optimal Pareto frontier and what was its Recall@5?",
                category = "answerable",
                groundTruthRelevantChunkIds = listOf("chk_doc_ra_1"),
                expectedAbstention = false,
                referenceAnswer = "Hybrid retrieval reached the optimal Pareto frontier, achieving Recall@5 of 0.91 and MRR of 0.86."
            ),
            BenchmarkSample(
                id = "q3",
                question = "What were the proprietary GPU cluster hyperparameters and exact power draw used during the training phase?",
                category = "unanswerable",
                groundTruthRelevantChunkIds = emptyList(),
                expectedAbstention = true,
                referenceAnswer = "The document does not disclose GPU cluster hyperparameters or power draw."
            ),
            BenchmarkSample(
                id = "q4",
                question = "Why does high cosine similarity not establish factual entailment in RAG verification?",
                category = "answerable",
                groundTruthRelevantChunkIds = listOf("chk_doc_ra_2"),
                expectedAbstention = false,
                referenceAnswer = "Semantic similarity measures topical overlap rather than logical entailment; high cosine similarity can occur even when a passage lacks necessary premises or contradicts the claim."
            ),
            BenchmarkSample(
                id = "q5",
                question = "What four conservative verification statuses are assigned to claims in the evaluation protocol?",
                category = "answerable",
                groundTruthRelevantChunkIds = listOf("chk_doc_ra_2"),
                expectedAbstention = false,
                referenceAnswer = "Supported, Contradicted, Insufficient Evidence, and Unverified."
            ),
            BenchmarkSample(
                id = "q6",
                question = "What was the precise human expert agreement rate with automated LLM evaluators across the 1,200 evaluated claims?",
                category = "answerable",
                groundTruthRelevantChunkIds = listOf("chk_doc_ra_3"),
                expectedAbstention = false,
                referenceAnswer = "Human expert audits showed an agreement rate of 88.4% with automated LLM evaluators."
            ),
            BenchmarkSample(
                id = "q7",
                question = "What was the exact proprietary commercial licensing fee charged by the authors for the benchmark dataset?",
                category = "unanswerable",
                groundTruthRelevantChunkIds = emptyList(),
                expectedAbstention = true,
                referenceAnswer = "The document does not contain information regarding commercial licensing fees."
            ),
            BenchmarkSample(
                id = "q8",
                question = "How should a faithful research assistant handle conflicting data between different studies according to the paper?",
                category = "conflicting",
                groundTruthRelevantChunkIds = listOf("chk_doc_ra_2"),
                expectedAbstention = false,
                referenceAnswer = "A faithful assistant must explicitly disclose the empirical disagreement and cite both sides rather than arbitrarily favoring one source."
            )
        )
    )
}
