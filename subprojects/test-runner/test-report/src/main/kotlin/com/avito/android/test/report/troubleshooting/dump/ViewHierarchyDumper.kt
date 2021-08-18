package com.avito.android.test.report.troubleshooting.dump

import radiography.Radiography
import radiography.ScanScopes.AllWindowsScope
import radiography.ViewStateRenderers

object ViewHierarchyDumper {

    fun getDump(): String {
        return Radiography.scan(
            scanScope = AllWindowsScope,
            viewStateRenderers = ViewStateRenderers.DefaultsIncludingPii
        )
    }
}
