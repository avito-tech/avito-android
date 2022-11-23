package com.avito.android.info.di

import com.avito.android.OwnerSerializer
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.owner.adapter.OwnerAdapter
import com.squareup.moshi.Moshi

internal class MoshiProvider(
    private val ownerAdapter: OwnerAdapter
) {

    constructor(ownersSerializer: OwnerSerializer) : this(DefaultOwnerAdapter(ownersSerializer))

    fun provide(): Moshi = Moshi.Builder().add(ownerAdapter).build()
}
