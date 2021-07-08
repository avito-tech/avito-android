package com.avito.runner.artifacts

import com.avito.report.TestArtifactsProvider
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import com.avito.report.model.Incident
import com.avito.report.model.Step
import com.avito.report.model.Video
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

internal class ReportArtifactsUploader(
    private val testArtifactsUploader: TestArtifactsUploader,
    private val testArtifactsProvider: TestArtifactsProvider
) {

    suspend fun processVideo(video: Video?): Video? {
        return if (video != null) {
            processFileAddress(video.fileAddress, Entry.File.Type.video)
                ?.let { video.copy(fileAddress = it) }
                ?: video
        } else {
            null
        }
    }

    suspend fun processStepList(
        stepList: List<Step>
    ): List<Deferred<Step>> {
        return withContext(currentCoroutineContext()) {
            stepList.map { step ->
                async {
                    step.copy(
                        entryList = processEntryList(step.entryList)
                            .map { it.await() }
                    )
                }
            }
        }
    }

    suspend fun processIncident(
        incident: Incident?
    ): Incident? {
        if (incident == null) return null
        return incident.copy(
            entryList = processEntryList(incident.entryList)
                .map { it.await() }
        )
    }

    private suspend fun processEntryList(
        entryList: List<Entry>
    ): List<Deferred<Entry>> {
        return entryList
            .map { entry -> processEntryAsync(entry) }
    }

    private suspend fun processEntryAsync(entry: Entry): Deferred<Entry> {
        return withContext(currentCoroutineContext()) {
            async {
                when (entry) {
                    is Entry.File -> processFileAddress(entry.fileAddress, entry.fileType)
                        ?.let { entry.copy(fileAddress = it) }
                        ?: entry
                    else -> entry
                }
            }
        }
    }

    /**
     * null means nothing changed, no need to create new copy of an object
     */
    private suspend fun processFileAddress(fileAddress: FileAddress, type: Entry.File.Type): FileAddress? {
        return when (fileAddress) {
            is FileAddress.File ->
                testArtifactsProvider.getFile(fileAddress.fileName)
                    .flatMap { fullPath -> testArtifactsUploader.upload(file = fullPath, type = type) }
                    .fold(
                        onSuccess = { url -> FileAddress.URL(url) },
                        onFailure = { throwable -> FileAddress.Error(throwable) }
                    )

            is FileAddress.URL,
            is FileAddress.Error -> null
        }
    }
}
