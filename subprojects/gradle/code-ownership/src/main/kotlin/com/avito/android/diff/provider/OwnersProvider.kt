package com.avito.android.diff.provider

import com.avito.android.model.Owner

public fun interface OwnersProvider {

    public fun get(): Set<Owner>
}
