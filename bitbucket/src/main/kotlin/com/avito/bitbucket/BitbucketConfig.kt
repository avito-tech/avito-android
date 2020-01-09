package com.avito.bitbucket

import java.io.Serializable

data class BitbucketConfig(
    val baseUrl: String,
    val credentials: AtlassianCredentials,
    val projectKey: String,
    val repositorySlug: String
) : Serializable
