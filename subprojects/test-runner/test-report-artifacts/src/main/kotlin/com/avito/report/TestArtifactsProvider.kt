package com.avito.report

import com.avito.android.Result
import java.io.File

/**
 * API to access test artifacts from different places:
 * - ExternalStorageTransport writes to getExternalFilesDirs(targetContenxt)
 * - DeviceWorker specifies path on device via createForAdbAccess()
 *   (to be more precise: /sdcard/Android/data/${appUnderTestPackage}files)
 * - ArtifactsTestListener creates tempDirectory and pulls its content using path provided by DeviceWorker
 * - TestArtifactsProcessor reads from tempDirectory and uploads multiple files in parallel but in blocking manner
 * - ArtifactsTestListener removes tempDirectory after that
 *
 * use [TestArtifactsProviderFactory] to create instances
 */
public interface TestArtifactsProvider {

    public val rootDir: Lazy<File>

    public fun provideReportDir(): Result<File>

    public fun provideReportFile(): Result<File>

    public fun getFile(relativePath: String): Result<File>

    public fun generateFile(name: String, extension: String, create: Boolean = false): Result<File>

    public fun generateUniqueFile(extension: String, create: Boolean = false): Result<File>
}
