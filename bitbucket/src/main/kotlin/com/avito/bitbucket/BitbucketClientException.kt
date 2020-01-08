package com.avito.bitbucket

class BitbucketClientException(
    override val message: String,
    override val cause: Throwable?
) : RuntimeException(message, cause)
