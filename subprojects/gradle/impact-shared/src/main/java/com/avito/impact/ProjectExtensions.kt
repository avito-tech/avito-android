package com.avito.impact

import com.avito.impact.fallback.ImpactFallbackDetector
import com.avito.impact.fallback.ImpactFallbackDetectorImplementation
import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope
import org.gradle.api.Project

public val Project.impactFallbackDetector: ImpactFallbackDetector
    by ProjectProperty.lazy(scope = PropertyScope.ROOT_PROJECT) { project ->
        ImpactFallbackDetectorImplementation.from(project)
    }
