package com.avito.report.internal

import com.avito.android.Result
import com.avito.report.TestArtifactsProvider
import com.avito.report.TestDirGenerator
import java.io.File

class UniqueDirTestArtifactsProvider(
    override val rootDir: Lazy<File>,
    private val testDirGenerator: TestDirGenerator,
) : TestArtifactsProvider {

    private val runnerOutputFolder = "runner"

    override fun provideReportDir(): Result<File> {
        val testFolderName = testDirGenerator.generateUniqueDir()

        val runnerDirectory = File(rootDir.value, runnerOutputFolder)

        return Result.tryCatch {
            File(runnerDirectory, testFolderName).apply {
                parentFile?.mkdirs()
                mkdir()
            }
        }
    }

    override fun provideReportFile(): Result<File> {
        return directTestArtifactsProvider().flatMap {
            it.provideReportFile()
        }
    }

    override fun getFile(relativePath: String): Result<File> {
        return directTestArtifactsProvider().flatMap {
            it.getFile(relativePath)
        }
    }

    override fun generateFile(name: String, extension: String, create: Boolean): Result<File> {
        return directTestArtifactsProvider().flatMap {
            it.generateFile(name, extension, create)
        }
    }

    override fun generateUniqueFile(extension: String, create: Boolean): Result<File> {
        return directTestArtifactsProvider().flatMap {
            it.generateUniqueFile(extension, create)
        }
    }

    private fun directTestArtifactsProvider(): Result<TestArtifactsProvider> {
        return provideReportDir().map {
            DirectTestArtifactsProvider(lazy { it })
        }
    }
}
