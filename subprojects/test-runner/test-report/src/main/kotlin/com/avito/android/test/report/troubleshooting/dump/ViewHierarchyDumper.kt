package com.avito.android.test.report.troubleshooting.dump

import radiography.Radiography
import radiography.ScanScopes.FocusedWindowScope
import radiography.ViewStateRenderers

object ViewHierarchyDumper {

    fun getDump(): String {
        return Radiography.scan(
            scanScope = FocusedWindowScope,
            viewStateRenderers = ViewStateRenderers.DefaultsIncludingPii
        )
    }
}
