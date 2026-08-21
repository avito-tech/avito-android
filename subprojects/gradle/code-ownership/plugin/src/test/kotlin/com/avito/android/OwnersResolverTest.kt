package com.avito.android

import com.avito.android.model.network.AvitoOwner
import com.avito.android.model.network.OwnerType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class OwnersResolverTest {

    private val resolver = OwnersResolver()

    @Test
    fun `unit with teams - names are normalized`() {
        val resolved = resolver.resolve(listOf(unit("Mobile Architecture", teams = listOf(team("bt clicks")))))

        assertThat(resolved.map { it.normalizedName }).containsExactly("Mobile_Architecture")
        assertThat(resolved.single().teams.map { it.normalizedName }).containsExactly("Bt_Clicks")
    }

    @Test
    fun `teams of different units have same name - second one is prefixed with unit name`() {
        val resolved = resolver.resolve(
            listOf(
                unit("Explore", teams = listOf(team("Speed"))),
                unit("Trust", teams = listOf(team("Speed"))),
            )
        )

        assertThat(resolved[0].teams.map { it.normalizedName }).containsExactly("Speed")
        assertThat(resolved[1].teams.map { it.normalizedName }).containsExactly("TrustSpeed")
    }

    @Test
    fun `teams of one unit have same name - second one is prefixed with unit name`() {
        val resolved = resolver.resolve(listOf(unit("Trust", teams = listOf(team("Speed"), team("Speed")))))

        assertThat(resolved.single().teams.map { it.normalizedName }).containsExactly("Speed", "TrustSpeed")
    }

    @Test
    fun `names with special characters and cyrillic - normalization is stable`() {
        val resolved = resolver.resolve(
            listOf(
                unit("чат-боты", teams = emptyList()),
                unit("Поиск & Реко", teams = emptyList()),
                unit("Avito (Услуги)", teams = emptyList()),
                unit("a/b\\c_d-e", teams = emptyList()),
                unit("Ы и ы", teams = emptyList()),
            )
        )

        assertThat(resolved.map { it.normalizedName })
            .containsExactly("CHat_Boty", "Poisk__And__Reko", "Avito_Uslugi", "A_B_C_D_E", "Ы_I_Y")
            .inOrder()
    }

    @Test
    fun `team keeps its owner - uuid is available for generators`() {
        val resolved = resolver.resolve(listOf(unit("Explore", teams = listOf(team("Speed", id = "uuid-1")))))

        assertThat(resolved.single().teams.single().owner.id).isEqualTo("uuid-1")
    }

    @Test
    fun `owner is a team on the top level - it is skipped`() {
        val resolved = resolver.resolve(listOf(team("Speed"), unit("Explore", teams = emptyList())))

        assertThat(resolved.map { it.normalizedName }).containsExactly("Explore")
    }

    private fun unit(name: String, teams: List<AvitoOwner>, id: String = "unit-id") = AvitoOwner(
        id = id,
        name = name,
        type = OwnerType.Unit,
        channels = emptyList(),
        children = teams.toMutableList(),
    )

    private fun team(name: String, id: String = "team-id") = AvitoOwner(
        id = id,
        name = name,
        type = OwnerType.Team,
        channels = emptyList(),
    )
}
