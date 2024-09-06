package com.avito.android.model

import java.io.Serializable

public interface AvitoCodeOwner : Owner {
    public val type: Type

    public val unitName: String
        get() = when (val type = this.type) {
            is Team -> type.unit.type.name
            is Unit -> type.name
        }

    public val unitType: Type
        get() = when (val type = this.type) {
            is Team -> type.unit.type
            is Unit -> type
        }
}

public sealed class Type(
    public val name: String,
    public val id: String,
) : Serializable

public class Unit(
    name: String,
    id: String,
) : Type(name, id)

public class Team(
    name: String,
    id: String,
    public val unit: AvitoCodeOwner,
) : Type(name, id)
