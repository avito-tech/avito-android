package com.avito.android.test.report.video

import com.avito.android.Result
import com.avito.report.TestArtifactsProvider
import java.io.File

internal class StubTestArtifactsProvider(
    private val reportDir: File
) : TestArtifactsProvider {

    private var fileIndex = 0

    override fun provideReportDir(): Result<File> = Result.Success(reportDir.apply { mkdirs() })

    override fun provideReportFile(): Result<File> = getFile("report.json")

    override fun getFile(relativePath: String): Result<File> = Result.Success(File(reportDir, relativePath))

    override fun generateFile(name: String, extension: String, create: Boolean): Result<File> =
        createFile("$name.$extension", create)

    override fun generateUniqueFile(extension: String, create: Boolean): Result<File> =
        createFile("file-${fileIndex++}.$extension", create)

    private fun createFile(name: String, create: Boolean): Result<File> {
        val file = File(reportDir.apply { mkdirs() }, name)
        if (create) {
            file.createNewFile()
        }
        return Result.Success(file)
    }
}
