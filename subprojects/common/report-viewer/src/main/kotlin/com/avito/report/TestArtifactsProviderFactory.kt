package com.avito.report

import com.avito.report.internal.DirectTestArtifactsProvider
import com.avito.report.internal.UniqueDirTestArtifactsProvider
import com.avito.report.model.TestStaticData
import java.io.File

object TestArtifactsProviderFactory {

    fun create(
        testReportRootDir: Lazy<File>,
        testStaticData: TestStaticData
    ): TestArtifactsProvider {
        return UniqueDirTestArtifactsProvider(
            rootDir = testReportRootDir,
            testDirGenerator = TestDirGenerator.StaticData(testStaticData)
        )
    }

    fun create(
        testReportRootDir: Lazy<File>,
        className: String,
        methodName: String
    ): TestArtifactsProvider {
        return UniqueDirTestArtifactsProvider(
            rootDir = testReportRootDir,
            testDirGenerator = TestDirGenerator.Impl(className, methodName)
        )
    }

    fun createForTempDir(tempDir: File): TestArtifactsProvider {
        return DirectTestArtifactsProvider(lazy { tempDir })
    }

    @Suppress("SdCardPath") // android API's are unavailable here
    fun createForAdbAccess(appUnderTestPackage: String, className: String, methodName: String): TestArtifactsProvider {
        val dataPath = "/sdcard/Android/data/${appUnderTestPackage}/files"
        return UniqueDirTestArtifactsProvider(
            rootDir = lazy { File(dataPath) },
            testDirGenerator = TestDirGenerator.Impl(
                className = className,
                methodName = methodName
            )
        )
    }
}
