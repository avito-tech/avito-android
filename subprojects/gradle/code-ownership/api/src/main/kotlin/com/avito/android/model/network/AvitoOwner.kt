package com.avito.android.model.network

public data class AvitoOwner(
    val id: String,
    val name: String,
    val type: OwnerType,
    val channels: List<String>,
    val children: MutableList<AvitoOwner> = mutableListOf(),
    val parentId: String? = null,
    val people: List<AvitoPeoplePerson> = emptyList(),
)

public data class AvitoPeoplePerson(
    val id: String,
    val email: String,
)

public enum class OwnerType(public val key: String) {
    Team("команда"),
    Unit("юнит");

    public companion object {
        public fun getByKey(key: String?): OwnerType {
            return when (key) {
                Team.key -> Team
                Unit.key -> Unit
                else -> error("Not available option for owner type: $key")
            }
        }
    }
}
