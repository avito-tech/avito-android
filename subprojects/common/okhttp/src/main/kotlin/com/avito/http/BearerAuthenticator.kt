package com.avito.http

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

public class BearerAuthenticator(
    private val token: String,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request = response.request
        .newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
}
