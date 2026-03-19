package com.example.newsapp.data.mapper

import com.example.newsapp.data.local.entity.ArticleEntity
import com.example.newsapp.data.remote.dto.ArticleDto
import com.example.newsapp.domain.model.Article
import kotlin.text.trim
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun ArticleDto.toEntity(isFavorite: Boolean = false): ArticleEntity =
    ArticleEntity(
        id = id,
        title = title,
        summary = summary.normalizeSummary(),
        url = url,
        imageUrl = imageUrl,
        newsSite = newsSite,
        publishedAt = publishedAt,
        isFavorite = isFavorite
    )

fun ArticleEntity.toDomain(): Article =
    Article(
        id = id,
        title = title,
        summary = summary,
        url = url,
        imageUrl = imageUrl,
        newsSite = newsSite,
        publishedAt = publishedAt?.let { formatPublishedAt(it) },
        isFavorite = isFavorite
    )

private fun String.normalizeSummary(): String =
    lineSequence()
        .dropWhile { it.isBlank() }
        .joinToString(separator = " ")
        .trim()

private fun formatPublishedAt(raw: String): String {
    return try {
        val parsed = OffsetDateTime.parse(raw)
        val formatter = DateTimeFormatter.ofPattern("MMM d, uuuu • HH:mm")
        parsed.format(formatter)
    } catch (e: DateTimeParseException) {
        raw
    }
}


