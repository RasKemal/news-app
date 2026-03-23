package com.example.newsapp.data.mapper

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ArticleMapperTest {

    @Test
    fun `normalizeSummary should remove multiple newlines and trim whitespace`() {
        val messySummary = """
            
            This is a summary.
            
            It has multiple lines.
            
        """.trimIndent()

        // This simulates the call to normalizeSummary() inside toEntity()
        val cleaned = messySummary.normalizeSummary()

        // ASSERT: All newlines should be replaced by a single space
        val expected = "This is a summary. It has multiple lines."
        assertEquals(expected, cleaned)
    }

    @Test
    fun `formatPublishedAt should return pretty date for valid ISO string`() {
        val rawDate = "2026-03-19T12:00:00Z"
        val formatted = formatPublishedAt(rawDate)

        // ASSERT: Check the pattern "MMM d, uuuu • HH:mm"
        assertEquals("Mar 19, 2026 • 12:00", formatted)
    }
}