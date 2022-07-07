package com.avito.android.diff.provider

import com.avito.android.model.Owner

public class SimpleOwnersProvider(private val owners: Set<Owner>) : OwnersProvider {
    override fun get(): Set<Owner> {
        return owners
    }
}
