package com.avito.android.runner.devices.internal

import java.io.File

public class EmulatorsLogsReporterProvider(
    private val logcatTags: Collection<String>,
    private val outputDir: File,
) {

    internal fun provide(tempLogcatDir: File): EmulatorsLogsReporter {
        return EmulatorsLogsReporterImpl(
            outputFolder = outputDir,
            logcatTags = logcatTags,
            logcatDir = tempLogcatDir
        )
    }
}
