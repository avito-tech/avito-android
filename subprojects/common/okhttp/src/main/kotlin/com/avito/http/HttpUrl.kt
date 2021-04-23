package com.avito.http

import com.avito.android.Result
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

public fun String.toHttpUrlResult(): Result<HttpUrl> {
    return try {
        Result.Success(toHttpUrl())
    } catch (e: IllegalArgumentException) {
        Result.Failure(IllegalArgumentException("Failed to parse httpUrl: $this", e))
    }
}
