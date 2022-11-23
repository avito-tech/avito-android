package com.avito.android.tech_budget.internal.di

import com.avito.android.OwnerSerializer
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.owner.adapter.OwnerAdapter
import com.squareup.moshi.Moshi
import javax.inject.Provider

internal class MoshiProvider(
    private val uploadOwnersAdapter: OwnerAdapter
) {

    constructor(ownersSerializer: Provider<OwnerSerializer>) : this(DefaultOwnerAdapter(ownersSerializer))

    fun provide(): Moshi = Moshi.Builder().add(uploadOwnersAdapter).build()
}
