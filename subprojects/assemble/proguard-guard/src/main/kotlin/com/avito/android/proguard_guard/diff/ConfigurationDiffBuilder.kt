package com.avito.android.proguard_guard.diff

import com.avito.android.diff_util.DiffAlgorithm
import com.avito.android.diff_util.EditList
import com.avito.android.diff_util.comparator.StringComparator
import com.avito.android.diff_util.sequence.toSequence

internal class ConfigurationDiffBuilder {

    fun build(
        lockedConfigurationLines: List<String>,
        mergedConfigurationLines: List<String>,
    ): EditList {
        val lockedConfigurationSequence = lockedConfigurationLines.toSequence()
        val mergedConfigurationSequence = mergedConfigurationLines.toSequence()

        val diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM)

        return diffAlgorithm.diff(
            StringComparator(),
            lockedConfigurationSequence,
            mergedConfigurationSequence
        )
    }
}
