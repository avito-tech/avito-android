package com.avito.android.model.output

import com.avito.android.model.input.Deployment
import okhttp3.HttpUrl

internal interface ArtifactsFactory<T : Deployment> {

    fun create(deployment: T, url: HttpUrl): Artifact
}
