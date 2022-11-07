package com.avito.android.http

import com.avito.http.BasicAuthenticator
import com.avito.http.RetryInterceptor
import okhttp3.OkHttpClient

internal fun createArtifactoryHttpClient(
    user: String,
    password: String,
): OkHttpClient {
    return OkHttpClient.Builder()
        .authenticator(BasicAuthenticator(user, password))
        .addInterceptor(RetryInterceptor(allowedMethods = listOf("PUT")))
        .build()
}
