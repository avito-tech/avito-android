package com.avito.report

import com.avito.android.Result
import java.io.File

interface TestArtifactsProvider {

    val rootDir: Lazy<File>

    fun provideReportDir(): Result<File>

    fun provideReportFile(): Result<File>

    fun getFile(relativePath: String): Result<File>

    fun generateFile(name: String, extension: String, create: Boolean = false): Result<File>

    fun generateUniqueFile(extension: String, create: Boolean = false): Result<File>
}
