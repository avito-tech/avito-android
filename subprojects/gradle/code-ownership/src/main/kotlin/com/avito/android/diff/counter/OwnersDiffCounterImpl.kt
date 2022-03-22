package com.avito.android.diff.counter

import com.avito.android.diff.comparator.OwnersComparator
import com.avito.android.diff.model.OwnersDiff
import com.avito.android.model.Owner

internal class OwnersDiffCounterImpl(
    private val comparator: OwnersComparator
) : OwnersDiffCounter {

    override fun countOwnersDiff(expectedOwners: Set<Owner>, actualOwners: Set<Owner>): OwnersDiff {
        val removed = mutableSetOf<Owner>()
        val added = mutableSetOf<Owner>()
        expectedOwners.forEach { expected ->
            if (actualOwners.none { actual -> comparator.isSame(expected, actual) }) {
                removed.add(expected)
            }
        }
        actualOwners.forEach { actual ->
            if (expectedOwners.none { expected -> comparator.isSame(expected, actual) }) {
                added.add(actual)
            }
        }
        return OwnersDiff(removed, added)
    }
}
