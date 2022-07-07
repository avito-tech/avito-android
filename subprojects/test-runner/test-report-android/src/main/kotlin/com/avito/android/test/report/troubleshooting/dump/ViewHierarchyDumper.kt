package com.avito.android.test.report.troubleshooting.dump

import radiography.Radiography
import radiography.ScanScopes.AllWindowsScope
import radiography.ViewStateRenderers

class ViewHierarchyDumper : Dumper {

    override val label: String = "View hierarchy dump"

    override fun dump(): String {
        return Radiography.scan(
            scanScope = AllWindowsScope,
            viewStateRenderers = ViewStateRenderers.DefaultsIncludingPii
        )
    }
}
