package com.avito.emcee.di.internal

import com.avito.emcee.internal.ArtifactoryFileUploader
import com.avito.emcee.internal.ArtifactorySettings
import com.avito.emcee.internal.BucketNameGenerator
import com.avito.emcee.internal.FileUploader
import com.avito.http.BasicAuthenticator
import okhttp3.OkHttpClient

internal class FileUploaderProvider(
    private val artifactorySettings: ArtifactorySettings,
    private val bucketNameGenerator: BucketNameGenerator
) {

    fun provide(client: OkHttpClient): FileUploader {
        return ArtifactoryFileUploader(
            httpClient = client
                .newBuilder()
                .authenticator(
                    BasicAuthenticator(artifactorySettings.user, artifactorySettings.password)
                )
                .build(),
            artifactorySettings = artifactorySettings,
            bucketName = bucketNameGenerator.generate()
        )
    }
}
