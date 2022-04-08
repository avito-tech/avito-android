package com.avito.android.diff.comparator

import com.avito.android.model.Owner

public interface OwnersComparator {
    public fun isSame(expected: Owner, actual: Owner): Boolean
}
