package com.avito.report.internal

import com.avito.android.Result
import com.avito.report.ReportDirProvider
import com.avito.report.TestArtifactsProvider
import java.io.File
import java.util.UUID

internal class DirectTestArtifactsProvider(
    private val provider: ReportDirProvider,
    private val uniqueFileNameGenerator: () -> String = { UUID.randomUUID().toString() }
) : TestArtifactsProvider {

    private val reportFileName = "report.json"

    override fun provideReportDir(): Result<File> {
        return provider.reportDir
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
            val file = File(dir, "$name.$extension")
            if (create && !file.exists()) {
                require(file.createNewFile()) {
                    "Can't generate file $file"
                }
            }
            file
        }
    }

    override fun generateUniqueFile(extension: String, create: Boolean): Result<File> {
        return generateFile(name = uniqueFileNameGenerator.invoke(), extension = extension, create = create)
    }
}
