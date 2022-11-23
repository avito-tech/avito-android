package com.avito.android.owner.dependency

import com.avito.android.OwnerSerializer
import com.avito.android.info.di.MoshiProvider
import com.squareup.moshi.adapter

public class JsonOwnedDependenciesSerializer(
    ownerSerializer: OwnerSerializer
) : OwnedDependenciesSerializer {

    @OptIn(ExperimentalStdlibApi::class)
    private val jsonAdapter = MoshiProvider(ownerSerializer).provide().adapter<List<OwnedDependency>>()

    override fun serialize(ownedDependencies: List<OwnedDependency>): String {
        return jsonAdapter.toJson(ownedDependencies)
    }

    override fun deserialize(rawDependenciesText: String): List<OwnedDependency> {
        return jsonAdapter.fromJson(rawDependenciesText) ?: listOf()
    }
}
