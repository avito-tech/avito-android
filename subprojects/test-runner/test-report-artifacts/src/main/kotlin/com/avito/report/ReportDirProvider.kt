package com.avito.report

import com.avito.android.Result
import java.io.File

public interface ReportDirProvider {
    public val reportDir: Result<File>
}
