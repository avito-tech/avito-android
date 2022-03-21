package com.avito.android.diff.counter

import com.avito.android.diff.model.OwnersDiff
import com.avito.android.model.Owner

public interface OwnersDiffCounter {

    public fun countOwnersDiff(expectedOwners: Set<Owner>, actualOwners: Set<Owner>): OwnersDiff
}
