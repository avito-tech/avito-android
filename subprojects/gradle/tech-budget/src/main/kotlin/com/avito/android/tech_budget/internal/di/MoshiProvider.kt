package com.avito.android.tech_budget.internal.di

import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.squareup.moshi.Moshi

internal class MoshiProvider(
    private val ownerAdapterFactory: OwnerAdapterFactory
) {

    fun provide(): Moshi = Moshi.Builder().add(ownerAdapterFactory.adapter).build()
}
