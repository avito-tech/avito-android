package com.avito.report.internal

import com.avito.android.Result
import com.avito.report.TestArtifactsProvider
import java.io.File
import java.util.UUID

internal class DirectTestArtifactsProvider(
    override val rootDir: Lazy<File>,
    private val uniqueFileNameGenerator: () -> String = { UUID.randomUUID().toString() }
) : TestArtifactsProvider {

    private val reportFileName = "report.json"

    override fun provideReportDir(): Result<File> {
        return Result.Success(rootDir.value)
    }

    override fun provideReportFile(): Result<File> {
        return provideReportDir().map { dir ->
            File(dir, reportFileName)
        }
    }

    override fun getFile(relativePath: String): Result<File> {
        return provideReportDir().map { dir ->
            File(dir, relativePath)
        }
    }

    override fun generateFile(name: String, extension: String, create: Boolean): Result<File> {
        return provideReportDir().map { dir ->
            File(dir, "$name.$extension")
        }
    }

    override fun generateUniqueFile(extension: String, create: Boolean): Result<File> {
        return generateFile(name = uniqueFileNameGenerator.invoke(), extension = extension, create = create)
    }
}
