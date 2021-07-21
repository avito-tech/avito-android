package com.avito.report

import com.avito.android.Result
import java.io.File

/**
 * API to access test artifacts from different places:
 *
 * 1. [ExternalStorageTransport] writes to external dir. See [TestRunEnvironment.outputDirectory]
 * 2. [DeviceWorker] specifies path on device via `createForAdbAccess()`
 * 3. [ArtifactsTestListener] creates `tempDirectory` and pulls its content using path provided by [DeviceWorker]
 * 4. [TestArtifactsProcessor] reads from `tempDirectory` and uploads multiple files in parallel but in blocking manner
 * 5. [ArtifactsTestListener] removes `tempDirectory` after that
 *
 * Use [TestArtifactsProviderFactory] to get an instance.
 */
@Suppress("KDocUnresolvedReference")
public interface TestArtifactsProvider {

    public fun provideReportDir(): Result<File>

    public fun provideReportFile(): Result<File>

    public fun getFile(relativePath: String): Result<File>

    public fun generateFile(name: String, extension: String, create: Boolean = false): Result<File>

    public fun generateUniqueFile(extension: String, create: Boolean = false): Result<File>
}
