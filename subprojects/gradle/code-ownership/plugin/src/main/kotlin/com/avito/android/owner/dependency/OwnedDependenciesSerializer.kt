package com.avito.android.owner.dependency

public interface OwnedDependenciesSerializer {

    public fun serialize(ownedDependencies: List<OwnedDependency>): String
    public fun deserialize(rawDependenciesText: String): List<OwnedDependency>
}
