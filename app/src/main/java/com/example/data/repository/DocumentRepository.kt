package com.example.data.repository

import com.example.data.model.DocumentChunk
import com.example.data.model.ResearchDocument
import com.example.data.rag.BenchmarkDatasets
import com.example.data.rag.DocumentChunker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages the local document library and chunk index.
 */
class DocumentRepository {

    private val _documents = MutableStateFlow<List<ResearchDocument>>(emptyList())
    val documents: StateFlow<List<ResearchDocument>> = _documents.asStateFlow()

    init {
        // Load initial peer-reviewed benchmark documents
        _documents.value = BenchmarkDatasets.getAllInitialDocuments()
    }

    fun getAllChunks(): List<DocumentChunk> {
        return _documents.value.flatMap { it.chunks }
    }

    fun getChunksForDocument(docId: String): List<DocumentChunk> {
        return _documents.value.find { it.id == docId }?.chunks ?: emptyList()
    }

    fun getDocument(docId: String): ResearchDocument? {
        return _documents.value.find { it.id == docId }
    }

    fun addDocument(
        title: String,
        content: String,
        source: String = "txt",
        chunkSizeChars: Int = 450,
        chunkOverlapChars: Int = 80
    ): ResearchDocument {
        val docId = "doc_${UUID.randomUUID().toString().take(8)}"
        val chunks = DocumentChunker.chunkText(
            documentId = docId,
            documentTitle = title,
            text = content,
            chunkSizeChars = chunkSizeChars,
            chunkOverlapChars = chunkOverlapChars
        )
        val newDoc = ResearchDocument(
            id = docId,
            title = title,
            source = source,
            rawText = content,
            pageCount = (chunks.maxOfOrNull { it.pageNumber } ?: 1),
            chunks = chunks,
            fileSizeBytes = content.toByteArray().size.toLong()
        )
        _documents.value = listOf(newDoc) + _documents.value
        return newDoc
    }

    fun deleteDocument(docId: String) {
        _documents.value = _documents.value.filter { it.id != docId }
    }
}
