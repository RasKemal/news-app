package com.example.newsapp.core.helpers

import androidx.annotation.StringRes
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.example.newsapp.R

data class ErrorText(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int
)

fun Throwable.toUIError(): ErrorText {
    return when (this) {
        is UnknownHostException,
        is ConnectException,
        is SocketTimeoutException -> {
            ErrorText(
                titleRes = R.string.error_no_internet_title,
                descriptionRes = R.string.error_no_internet_description
            )
        }
        // sql crashes, unexpected http codes
        else -> {
            ErrorText(
                titleRes = R.string.error_generic_title,
                descriptionRes = R.string.error_generic_description
            )
        }
    }
}