package com.avito.report

import com.avito.report.model.TestStaticData
import java.io.File

public object TestArtifactsProviderFactory {

    public fun create(
        testReportRootDir: Lazy<File>,
        testStaticData: TestStaticData
    ): TestArtifactsProvider {
        return UniqueDirTestArtifactsProvider(
            rootDir = testReportRootDir,
            testDirGenerator = TestDirGenerator.StaticData(testStaticData)
        )
    }

    public fun create(
        testReportRootDir: Lazy<File>,
        className: String,
        methodName: String
    ): TestArtifactsProvider {
        return UniqueDirTestArtifactsProvider(
            rootDir = testReportRootDir,
            testDirGenerator = TestDirGenerator.Impl(className, methodName)
        )
    }

    public fun createForTempDir(tempDir: File): TestArtifactsProvider {
        return DirectTestArtifactsProvider(lazy { tempDir })
    }

    @Suppress("SdCardPath")
    public // android API's are unavailable here
    fun createForAdbAccess(appUnderTestPackage: String, className: String, methodName: String): TestArtifactsProvider {
        val dataPath = "/sdcard/Android/data/$appUnderTestPackage/files"
        return UniqueDirTestArtifactsProvider(
            rootDir = lazy { File(dataPath) },
            testDirGenerator = TestDirGenerator.Impl(
                className = className,
                methodName = methodName
            )
        )
    }
}
