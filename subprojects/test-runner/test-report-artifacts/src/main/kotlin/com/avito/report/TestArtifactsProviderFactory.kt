package com.avito.report

import com.avito.report.internal.DirectTestArtifactsProvider
import com.avito.report.internal.ReportDirProviderForAdb
import com.avito.report.internal.ReportDirProviderWithCreation
import com.avito.report.internal.SimpleDirProvider
import com.avito.test.model.TestName
import java.io.File

public object TestArtifactsProviderFactory {

    public fun createForAndroidRuntime(
        appDirProvider: ApplicationDirProvider,
        name: TestName
    ): TestArtifactsProvider {
        return DirectTestArtifactsProvider(
            provider = ReportDirProviderWithCreation(
                rootDir = appDirProvider,
                testDirGenerator = TestDirGenerator.Impl(name)
            )
        )
    }

    public fun createForTempDir(tempDir: File): TestArtifactsProvider =
        create(SimpleDirProvider(tempDir))

    private fun create(provider: ReportDirProvider) =
        DirectTestArtifactsProvider(provider)

    // Android API's are unavailable here
    @Suppress("SdCardPath")
    public fun createForAdb(
        appDirProvider: ApplicationDirProvider,
        name: TestName,
    ): TestArtifactsProvider {
        return create(
            ReportDirProviderForAdb(
                rootDir = appDirProvider,
                testDirGenerator = TestDirGenerator.Impl(
                    name = name
                )
            )
        )
    }
}
