package com.avito.android.diff.formatter

import com.avito.android.diff.model.OwnersDiff
import com.avito.android.model.AvitoCodeOwner
import com.avito.android.model.Owner
import com.avito.android.model.Team
import com.avito.android.model.Type
import com.avito.android.model.Unit
import com.avito.android.model.network.OwnerType
import com.avito.android.providers.RemoteAvitoCodeOwner
import com.avito.capitalize

internal class ChatMessageFormatter : OwnersDiffMessageFormatter {

    private val Owner.printableName: String
        get() {
            val typeName: String
            val ownerName: String
            val parentName: String?

            when (this) {
                is RemoteAvitoCodeOwner -> {
                    typeName = type.key
                    ownerName = name
                    parentName = parent
                }

                is AvitoCodeOwner -> {
                    typeName = type.printableName
                    ownerName = type.name
                    parentName = if (type is Team) unitName else null
                }

                else -> return this.toString()
            }

            return "${typeName.capitalize()} ${ownerName}${parentName?.let { " (Юнит $it)" } ?: ""}"
        }

    private val Type.printableName: String
        get() = when (this) {
            is Team -> OwnerType.Team.key
            is Unit -> OwnerType.Unit.key
        }

    override fun formatDiffMessage(diffs: OwnersDiff): String {

        return if (diffs.isEmpty()) {
            "Струкутра Code Owners в Android не изменилась :ok_hand:"
        } else {
            buildString {
                append(":warning: Изменилась структура Code Owners в Android! @android-mob-arch-team\n")
                if (diffs.removed.isNotEmpty()) {
                    val removedOwners = diffs.removed.joinToString { it.printableName }
                    append("*Удалились:* $removedOwners\n")
                }
                if (diffs.added.isNotEmpty()) {
                    val addedOwners = diffs.added.joinToString { it.printableName }
                    append("*Добавились:* $addedOwners\n")
                }
                append("Дежурный должен обновить список владельцев по гайду: $CODE_OWNERSHIP_DOC")
            }
        }
    }

    private companion object {
        const val CODE_OWNERSHIP_DOC = "http://links.k.avito.ru/wvl"
    }
}
