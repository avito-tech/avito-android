package com.avito.report

import com.avito.report.model.TestStaticData
import java.io.File

public object TestArtifactsProviderFactory {

    public fun create(
        testReportRootDir: Lazy<File>,
        testStaticData: TestStaticData
    ): TestArtifactsProvider {
        return create(
            testReportRootDir,
            testStaticData.name.className,
            testStaticData.name.methodName,
        )
    }

    public fun create(
        testReportRootDir: Lazy<File>,
        className: String,
        methodName: String
    ): TestArtifactsProvider {
        return DirectTestArtifactsProvider(
            provider = ReportDirProviderWithCreation(
                rootDir = testReportRootDir,
                testDirGenerator = TestDirGenerator.Impl(className, methodName)
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
        className: String,
        methodName: String
    ): TestArtifactsProvider {
        val dataPath = "/sdcard/Android/data/$appUnderTestPackage/files"
        return create(
            ReportDirProviderForAdb(
                rootDir = lazy { File(dataPath) },
                testDirGenerator = TestDirGenerator.Impl(
                    className = className,
                    methodName = methodName
                )

            )
        )
    }
}
