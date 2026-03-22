package com.example.newsapp.core.helpers

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(FlowPreview::class)
fun Flow<String>.prepareForSearch(debounceTimeout: Long = 300L): Flow<String?> {
    return this
        .map { raw ->
            raw.trim()
                .replace(Regex("[^\\p{L}\\p{N} ]+"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
        .debounce { cleanString ->
            if (cleanString.isBlank()) 0L else debounceTimeout
        }
        .distinctUntilChanged()
        .map { it.ifBlank { null } }
}
