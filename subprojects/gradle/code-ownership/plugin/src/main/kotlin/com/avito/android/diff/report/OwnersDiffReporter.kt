package com.avito.android.diff.report

import com.avito.android.diff.model.OwnersDiff

public fun interface OwnersDiffReporter {
    public fun reportDiffFound(diffs: OwnersDiff)
}
