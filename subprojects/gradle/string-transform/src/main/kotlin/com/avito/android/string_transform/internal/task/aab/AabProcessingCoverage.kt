package com.avito.android.string_transform.internal.task.aab

import java.io.File

internal class AabProcessingCoverage(
    private val workspaceDirectory: File,
) {

    private val handledPaths = mutableSetOf<String>()
    private val skippedPaths = mutableSetOf<String>()

    fun markHandled(file: File) {
        handledPaths += relativePath(file)
    }

    fun markSkipped(file: File) {
        skippedPaths += relativePath(file)
    }

    fun isResidualCandidate(file: File): Boolean {
        val relativePath = relativePath(file)
        return relativePath !in handledPaths && relativePath !in skippedPaths
    }

    private fun relativePath(file: File): String {
        return file.relativeTo(workspaceDirectory).invariantSeparatorsPath
    }
}
