package com.avito.instrumentation.internal

internal fun stubPem(type: String): String = "-----BEGIN $type-----\nstub\n-----END $type-----"
