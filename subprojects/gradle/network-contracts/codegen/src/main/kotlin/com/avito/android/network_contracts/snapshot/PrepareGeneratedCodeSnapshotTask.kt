package com.avito.android.network_contracts.snapshot

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.OutputDirectory
import java.io.File

internal abstract class PrepareGeneratedCodeSnapshotTask : Copy() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    override fun getDestinationDir(): File {
        return outputDirectory.get().asFile
    }

    companion object {
        const val NAME = "prepareCodegenSnapshot"
    }
}
