package com.avito.report

import com.avito.android.Result
import java.io.File

interface ReportFileProvider {

    val rootDir: Lazy<File>

    fun provideReportDir(): File

    fun provideReportFile(): File

    fun getFile(relativePath: String): File

    fun generateFile(name: String, extension: String, create: Boolean = false): Result<File>

    fun generateUniqueFile(extension: String, create: Boolean = false): Result<File>
}
