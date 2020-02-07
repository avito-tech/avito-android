package com.avito.instrumentation.impact

import com.avito.impact.util.RootId
import com.avito.impact.util.Screen
import com.avito.impact.util.Test
import com.avito.instrumentation.impact.model.AffectedTest

internal data class BytecodeAnalyzeSummary(
    val testsByScreen: Map<Screen, Set<Test>>,
    val testsAffectedByDependentOnUserChangedCode: Set<AffectedTest>,
    val testsModifiedByUser: Set<AffectedTest>,
    val rootIdByScreen: Map<Screen, RootId>
)
