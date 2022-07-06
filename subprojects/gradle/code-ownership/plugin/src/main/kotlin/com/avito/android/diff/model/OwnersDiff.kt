package com.avito.android.diff.model

import com.avito.android.model.Owner

public class OwnersDiff(
    public val removed: Set<Owner>,
    public val added: Set<Owner>
) : Collection<Owner> by (removed + added)
