package com.avito.android.artifactory_backup

import com.avito.android.http.createArtifactoryHttpClient
import com.avito.android.stats.StatsDConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

public abstract class ArtifactoryPublishTask : DefaultTask() {

    @get:Input
    public abstract val artifactoryUrl: Property<String>

    @get:Input
    public abstract val artifactoryUser: Property<String>

    @get:Input
    public abstract val artifactoryPassword: Property<String>

    @get:Input
    public abstract val repository: Property<String>

    @get:Input
    public abstract val projectName: Property<String>

    @get:Input
    public abstract val projectType: Property<String>

    @get:Input
    public abstract val version: Property<String>

    @get:InputFiles
    public abstract val files: Property<FileCollection>

    @get:Internal
    public abstract val statsDConfig: Property<StatsDConfig>

    @TaskAction
    public fun doWork() {
        val httpClient = createArtifactoryHttpClient(
            user = artifactoryUser.get(),
            password = artifactoryPassword.get(),
            statsDConfig = statsDConfig.get()
        )

        val destinationFolder = artifactoryUrl.get()
            .toHttpUrl()
            .newBuilder()
            .addEncodedPathSegment(repository.get())
            .addEncodedPathSegment(projectName.get())
            .addEncodedPathSegment(projectType.get())
            .addEncodedPathSegment(version.get())

        files.get().forEach { file ->

            val request = Request.Builder()
                .put(file.asRequestBody())
                .url(destinationFolder.addEncodedPathSegment(file.name).build())
                .build()

            httpClient.newCall(request).execute()
        }
    }
}
