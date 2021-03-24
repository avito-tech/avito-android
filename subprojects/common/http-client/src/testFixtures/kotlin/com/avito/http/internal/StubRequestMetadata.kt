package com.avito.http.internal

public fun RequestMetadata.Companion.createStubInstance(
    serviceName: String = "some-service",
    methodName: String = "some-method"
): RequestMetadata = RequestMetadata(serviceName, methodName)
