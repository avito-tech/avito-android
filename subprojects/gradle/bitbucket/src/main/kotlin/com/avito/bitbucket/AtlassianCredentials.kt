package com.avito.bitbucket

import java.io.Serializable

public data class AtlassianCredentials(
    val user: String,
    val password: String
) : Serializable
