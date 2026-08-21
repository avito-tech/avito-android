package com.avito.android

import com.avito.android.model.network.AvitoOwner
import com.avito.android.model.network.OwnerType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class CommandUuidsGeneratorTest {

    private val generator = CommandUuidsGenerator()

    @Test
    fun `single team - constant is generated`() {
        val file = generator.generate(listOf(team("Speed", uuid = "1"))).toString()

        assertThat(file).contains("public object CommandUuids {")
        assertThat(file).contains("""public const val Speed: String = "1"""")
    }

    @Test
    fun `several teams - constants are sorted by name`() {
        val file = generator.generate(
            listOf(
                team("Speed", uuid = "1"),
                team("Deimos", uuid = "2"),
                team("Phobos", uuid = "3"),
            )
        ).toString()

        assertThat(file.indexOf("Deimos")).isLessThan(file.indexOf("Phobos"))
        assertThat(file.indexOf("Phobos")).isLessThan(file.indexOf("Speed"))
    }

    @Test
    fun `name is not a valid identifier - constant is escaped`() {
        val file = generator.generate(listOf(team("5SP", uuid = "1"), team("object", uuid = "2"))).toString()

        assertThat(file).contains("public const val `5SP`: String")
        assertThat(file).contains("public const val `object`: String")
    }

    @Test
    fun `constant name is empty - team is skipped`() {
        val file = generator.generate(listOf(team("", uuid = "1"), team("Speed", uuid = "2"))).toString()

        assertThat(file).contains("public const val Speed: String")
        assertThat(file).doesNotContain(""""1"""")
    }

    @Test
    fun `constant name consists of underscores - team is skipped`() {
        val file = generator.generate(
            listOf(team("_", uuid = "1"), team("__", uuid = "2"), team("Speed", uuid = "3"))
        ).toString()

        assertThat(file).contains("public const val Speed: String")
        assertThat(file).doesNotContain("`_`")
        assertThat(file).doesNotContain("`__`")
    }

    @Test
    fun `same constant name twice - last uuid wins, as in the owners enum`() {
        val file = generator.generate(listOf(team("Speed", uuid = "1"), team("Speed", uuid = "2"))).toString()

        assertThat(file).contains("""public const val Speed: String = "2"""")
        assertThat(file).doesNotContain(""""1"""")
    }

    @Test
    fun `no teams - object is empty`() {
        val file = generator.generate(emptyList()).toString()

        assertThat(file).contains("public object CommandUuids")
        assertThat(file).doesNotContain("const val")
    }

    private fun team(normalizedName: String, uuid: String) = ResolvedTeam(
        normalizedName = normalizedName,
        owner = AvitoOwner(
            id = uuid,
            name = "$normalizedName as it is named in Avito People",
            type = OwnerType.Team,
            channels = emptyList(),
        )
    )
}
