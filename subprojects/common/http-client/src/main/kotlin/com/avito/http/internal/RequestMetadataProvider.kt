package com.avito.http.internal

import com.avito.android.Result
import okhttp3.Request

public interface RequestMetadataProvider {

    public fun provide(request: Request): Result<RequestMetadata>
}
