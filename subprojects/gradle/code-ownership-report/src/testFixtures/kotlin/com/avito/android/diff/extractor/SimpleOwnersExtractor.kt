package com.avito.android.diff.extractor

import com.avito.android.model.Owner

public class SimpleOwnersExtractor(private val owners: Set<Owner>) : OwnersExtractor {
    override fun extractOwners(): Set<Owner> {
        return owners
    }
}
