package com.avito.bitbucket

internal class BitbucketClientException(
    override val message: String,
    override val cause: Throwable?
) : RuntimeException(message, cause)
