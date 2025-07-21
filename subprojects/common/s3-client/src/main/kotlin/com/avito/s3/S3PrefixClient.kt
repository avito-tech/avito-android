package com.avito.s3

import com.avito.android.Result
import java.io.File
import java.net.URL

public class S3PrefixClient(
    private val delegate: S3Client,
    private val objectsPrefix: String,
) : S3Client {
    override suspend fun putObject(key: String, objekt: File): Result<URL> {
        return delegate.putObject("$objectsPrefix/$key", objekt)
    }
}
