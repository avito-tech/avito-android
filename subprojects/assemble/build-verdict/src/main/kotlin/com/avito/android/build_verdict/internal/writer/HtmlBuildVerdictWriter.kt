package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.logger.Logger
import org.gradle.api.provider.Provider
import java.io.File

internal class HtmlBuildVerdictWriter(
    buildVerdictDir: Lazy<File>,
    override val logger: Provider<Logger>
) : BuildVerdictWriter(buildVerdictDir, fileName) {

    override fun writeTo(buildVerdict: BuildVerdict, destination: File) {
        destination.writeText(
            when (buildVerdict) {
                is BuildVerdict.Execution -> buildVerdict.html()
                is BuildVerdict.Configuration -> buildVerdict.html()
            }
        )
    }

    companion object {
        const val fileName = "build-verdict.html"
    }
}
