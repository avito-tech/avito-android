package com.avito.bitbucket

import java.io.Serializable

public sealed interface AtlassianCredentials : Serializable {

    public data class Basic(
        val user: String,
        val password: String
    ) : AtlassianCredentials

    public data class BearerToken(
        val token: String
    ) : AtlassianCredentials
}
