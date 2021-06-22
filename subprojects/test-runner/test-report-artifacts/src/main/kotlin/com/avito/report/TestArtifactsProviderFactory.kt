package com.avito.report

import com.avito.report.model.TestStaticData
import com.avito.test.model.TestName
import java.io.File

public object TestArtifactsProviderFactory {

    public fun create(
        testReportRootDir: Lazy<File>,
        testStaticData: TestStaticData
    ): TestArtifactsProvider {
        return create(
            testReportRootDir,
            testStaticData.name
        )
    }

    public fun create(
        testReportRootDir: Lazy<File>,
        name: TestName
    ): TestArtifactsProvider {
        return DirectTestArtifactsProvider(
            provider = ReportDirProviderWithCreation(
                rootDir = testReportRootDir,
                testDirGenerator = TestDirGenerator.Impl(name)
            )
        )
    }

    public fun createForTempDir(tempDir: File): TestArtifactsProvider =
        create(SimpleDirProvider(tempDir))

    private fun create(provider: ReportDirProvider) =
        DirectTestArtifactsProvider(provider)

    // android API's are unavailable here
    @Suppress("SdCardPath")
    public fun createForAdbAccess(
        appUnderTestPackage: String,
        name: TestName,
    ): TestArtifactsProvider {
        val dataPath = "/sdcard/Android/data/$appUnderTestPackage/files"
        return create(
            ReportDirProviderForAdb(
                rootDir = lazy { File(dataPath) },
                testDirGenerator = TestDirGenerator.Impl(
                    name = name
                )

            )
        )
    }
}
