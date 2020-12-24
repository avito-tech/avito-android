package com.avito.http

import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class BasicAuthenticator(private val user: String, private val password: String) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request = response.request
        .newBuilder()
        .addHeader("Authorization", Credentials.basic(user, password))
        .build()
}
