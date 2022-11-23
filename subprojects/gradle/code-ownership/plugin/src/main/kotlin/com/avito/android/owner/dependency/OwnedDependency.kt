package com.avito.android.owner.dependency

import com.avito.android.model.Owner
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class OwnedDependency(
    @Json(name = "moduleName") public val name: String,
    @Json(name = "owners") public val owners: Collection<Owner>,
    @Json(name = "type") public val type: Type
) {

    public enum class Type {
        @Json(name = "internal")
        INTERNAL,

        @Json(name = "external")
        EXTERNAL
    }
}
