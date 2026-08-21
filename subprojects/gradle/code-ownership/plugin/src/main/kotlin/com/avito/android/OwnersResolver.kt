package com.avito.android

import com.avito.android.model.network.AvitoOwner
import com.avito.android.model.network.OwnerType
import com.avito.android.utils.cyrillicToLatinAlphabet
import java.util.Locale

internal data class ResolvedTeam(
    val normalizedName: String,
    val owner: AvitoOwner,
)

internal data class ResolvedUnit(
    val normalizedName: String,
    val owner: AvitoOwner,
    val teams: List<ResolvedTeam>,
)

/**
 * Turns owners from Avito.People into names for generated declarations,
 * so that every generator uses the same naming.
 */
internal class OwnersResolver {

    fun resolve(remoteOwners: List<AvitoOwner>): List<ResolvedUnit> {
        val takenTeamNames = mutableSetOf<String>()

        return remoteOwners
            .filter { owner -> owner.type == OwnerType.Unit }
            .map { unit ->
                val unitName = unit.name.normalizeName()

                ResolvedUnit(
                    normalizedName = unitName,
                    owner = unit,
                    teams = unit.children.map { team ->
                        val teamName = team.name.normalizeName()
                        val normalizedName = if (teamName in takenTeamNames) unitName + teamName else teamName

                        takenTeamNames.add(normalizedName)

                        ResolvedTeam(normalizedName = normalizedName, owner = team)
                    }
                )
            }
    }

    private fun String.normalizeName(): String {
        val words = replace("&", "_And_")
            .replace("Ƞ", "Eta")
            .replace("Ω", "Omega")
            .replace("\t", "")
            .replace(".", "")
            .replace("(", "")
            .replace(")", "")
            .split(" ", "/", "\\", "_", "-")
        return words.joinToString("_") {
            val sb = StringBuilder()
            it.forEachIndexed { index, c ->
                val currentChar = c.toString()
                val cyrillicChar: String? = cyrillicToLatinAlphabet[currentChar]
                val newChar = cyrillicChar ?: currentChar
                sb.append(if (index == 0) newChar.uppercase(Locale.ROOT) else newChar)
            }
            sb.toString()
        }
    }
}
