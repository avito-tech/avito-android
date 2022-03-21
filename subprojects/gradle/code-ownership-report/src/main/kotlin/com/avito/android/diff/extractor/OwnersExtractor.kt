package com.avito.android.diff.extractor

import com.avito.android.model.Owner

public fun interface OwnersExtractor {

    public fun extractOwners(): Set<Owner>
}
