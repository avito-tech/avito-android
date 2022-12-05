package com.avito.android.proguard_guard.diff

import com.avito.android.diff_util.Edit
import com.avito.android.diff_util.EditList

internal class EditListFormatter(
    private val lockedConfigurationLines: List<String>,
    private val mergedConfigurationLines: List<String>,
) {

    internal fun format(editList: EditList): String {
        val stringBuilder = StringBuilder()
        editList.forEachIndexed { index, edit ->
            when (edit.type) {
                Edit.Type.INSERT -> insert(stringBuilder, mergedBegin = edit.beginB, mergedEnd = edit.endB)
                Edit.Type.DELETE -> delete(stringBuilder, referenceBegin = edit.beginA, referenceEnd = edit.endA)
                Edit.Type.REPLACE -> replace(
                    stringBuilder = stringBuilder,
                    referenceBegin = edit.beginA,
                    referenceEnd = edit.endA,
                    mergedBegin = edit.beginB,
                    mergedEnd = edit.endB
                )
                Edit.Type.EMPTY -> Unit
                null -> Unit
            }
            if (index != editList.lastIndex) {
                stringBuilder.append("...\n")
            }
        }
        return stringBuilder.toString()
    }

    private fun insert(stringBuilder: StringBuilder, mergedBegin: Int, mergedEnd: Int) {
        mergedConfigurationLines.subList(mergedBegin, mergedEnd).forEach { line ->
            stringBuilder.append("+++ $line\n")
        }
    }

    private fun delete(stringBuilder: StringBuilder, referenceBegin: Int, referenceEnd: Int) {
        lockedConfigurationLines.subList(referenceBegin, referenceEnd).forEach { line ->
            stringBuilder.append("--- $line\n")
        }
    }

    private fun replace(
        stringBuilder: StringBuilder,
        referenceBegin: Int,
        referenceEnd: Int,
        mergedBegin: Int,
        mergedEnd: Int,
    ) {
        delete(stringBuilder, referenceBegin, referenceEnd)
        insert(stringBuilder, mergedBegin, mergedEnd)
    }
}
