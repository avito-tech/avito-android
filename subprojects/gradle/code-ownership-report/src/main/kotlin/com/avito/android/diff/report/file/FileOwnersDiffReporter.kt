package com.avito.android.diff.report.file

import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.model.OwnersDiff
import com.avito.android.diff.report.OwnersDiffReporter
import java.io.File

public class FileOwnersDiffReporter(
    private val parentDir: File,
    private val messageFormatter: OwnersDiffMessageFormatter
) : OwnersDiffReporter {

    override fun reportDiffFound(diffs: OwnersDiff) {
        if (diffs.isEmpty()) return
        val file = File(parentDir, "ownership_diff_report.txt").apply {
            if (exists()) delete()
            createNewFile()
        }
        val formattedOwners = messageFormatter.formatDiffMessage(diffs)
        file.appendText(formattedOwners)
    }
}
