package com.avito.android.diff.formatter

import com.avito.android.diff.model.OwnersDiff

public interface OwnersDiffMessageFormatter {

    public fun formatDiffMessage(diffs: OwnersDiff): String
}
