package com.avito.emcee.worker.internal.artifacts

import okhttp3.HttpUrl

internal class FileNameResolver {

    fun resolve(httpUrl: HttpUrl): String = httpUrl.pathSegments.last()
}
