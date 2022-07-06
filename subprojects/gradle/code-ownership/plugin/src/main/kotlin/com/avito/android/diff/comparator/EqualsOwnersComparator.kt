package com.avito.android.diff.comparator

import com.avito.android.model.Owner

internal class EqualsOwnersComparator : OwnersComparator {
    override fun isSame(expected: Owner, actual: Owner): Boolean {
        return expected == actual
    }
}
