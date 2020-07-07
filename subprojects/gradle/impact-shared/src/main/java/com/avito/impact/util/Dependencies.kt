package com.avito.impact.util

import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Category

fun ModuleDependency.isUnknown() = (category == null)
fun ModuleDependency.isLibrary() = (category?.name == Category.LIBRARY)
fun ModuleDependency.isPlatform() = (category?.name == Category.REGULAR_PLATFORM)

private val ModuleDependency.category
    get() = attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)
