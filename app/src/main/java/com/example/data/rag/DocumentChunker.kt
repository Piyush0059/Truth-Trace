package com.example.data.rag

import com.example.data.model.DocumentChunk
import java.util.UUID

/**
 * Splits document text into overlapping chunks while preserving sentence boundaries and metadata.
 */
object DocumentChunker {

    fun chunkText(
        documentId: String,
        documentTitle: String,
        text: String,
        chunkSizeChars: Int = 450,
        chunkOverlapChars: Int = 80
    ): List<DocumentChunk> {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return emptyList()

        if (cleanText.length <= chunkSizeChars) {
            return listOf(
                DocumentChunk(
                    id = "chk_${documentId.take(6)}_0",
                    documentId = documentId,
                    documentTitle = documentTitle,
                    pageNumber = 1,
                    chunkIndex = 0,
                    text = cleanText,
                    startCharOffset = 0,
                    endCharOffset = cleanText.length
                )
            )
        }

        val chunks = mutableListOf<DocumentChunk>()
        var start = 0
        var chunkIndex = 0

        while (start < cleanText.length) {
            var end = (start + chunkSizeChars).coerceAtMost(cleanText.length)

            // Try to snap to sentence or paragraph boundary if within reasonable distance
            if (end < cleanText.length) {
                val searchWindow = cleanText.substring(
                    (end - 60).coerceAtLeast(start),
                    (end + 60).coerceAtMost(cleanText.length)
                )
                val breakIdx = searchWindow.lastIndexOfAny(charArrayOf('.', '\n', '!', '?'))
                if (breakIdx != -1) {
                    val adjustedEnd = (end - 60).coerceAtLeast(start) + breakIdx + 1
                    if (adjustedEnd > start + 100) {
                        end = adjustedEnd
                    }
                }
            }

            val chunkContent = cleanText.substring(start, end).trim()
            if (chunkContent.isNotEmpty()) {
                // Estimate page number based on standard 1800 chars per academic page
                val estimatedPage = (start / 1800) + 1
                chunks.add(
                    DocumentChunk(
                        id = "chk_${documentId.take(6)}_$chunkIndex",
                        documentId = documentId,
                        documentTitle = documentTitle,
                        pageNumber = estimatedPage,
                        chunkIndex = chunkIndex,
                        text = chunkContent,
                        startCharOffset = start,
                        endCharOffset = end
                    )
                )
                chunkIndex++
            }

            val advance = (end - start - chunkOverlapChars).coerceAtLeast(50)
            start += advance
        }

        return chunks
    }
}
