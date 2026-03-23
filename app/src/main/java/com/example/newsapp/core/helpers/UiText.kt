package com.example.newsapp.core.helpers

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    data class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText()
}

fun UiText.asString(context: Context): String =
    when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> context.getString(resId, *args.toTypedArray())
    }

