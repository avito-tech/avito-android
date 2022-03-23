package com.avito.emcee.internal

import java.util.UUID

internal object UUIDBucketNameGenerator : BucketNameGenerator {

    override fun generate(): String = UUID.randomUUID().toString()
}
