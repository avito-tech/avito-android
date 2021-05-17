package com.avito.instrumentation.internal.report.listener

import com.avito.report.TestArtifactsProvider
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import com.avito.report.model.Incident
import com.avito.report.model.Step
import com.avito.report.model.Video

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
    ): List<Step> {
        return stepList.map { step ->
            step.copy(
                entryList = processEntryList(step.entryList)
            )
        }
    }

    suspend fun processIncident(
        incident: Incident?
    ): Incident? {
        return incident?.copy(entryList = processEntryList(incident.entryList)) ?: incident
    }

    private suspend fun processEntryList(
        entryList: List<Entry>
    ): List<Entry> {
        return entryList.map { entry -> processEntry(entry) }
    }

    private suspend fun processEntry(entry: Entry): Entry {
        return when (entry) {
            is Entry.File -> processFileAddress(entry.fileAddress, entry.fileType)
                ?.let { entry.copy(fileAddress = it) }
                ?: entry
            else -> entry
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
