package com.avito.http.internal

import com.avito.android.Result
import okhttp3.Request

internal class TagRequestMetadataProvider : RequestMetadataProvider {

    override fun provide(request: Request): Result<RequestMetadata> {
        return try {
            val metadata = request.tag(RequestMetadata::class.java)
            if (metadata != null) {
                Result.Success(metadata)
            } else {
                Result.Failure(RuntimeException("Tag for 'RequestMetadata' not found in the request"))
            }
        } catch (e: ClassCastException) {
            Result.Failure(e)
        }
    }
}
