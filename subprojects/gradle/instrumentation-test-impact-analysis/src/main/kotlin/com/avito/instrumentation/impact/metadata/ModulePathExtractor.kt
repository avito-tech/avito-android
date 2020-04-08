package com.avito.instrumentation.impact.metadata

interface ModulePathExtractor {

    fun extract(targetClasses: Collection<String>): Set<ScreenToModulePath>

    class Impl(private val fieldName: String) : ModulePathExtractor {

        override fun extract(targetClasses: Collection<String>): Set<ScreenToModulePath> {
            return targetClasses.map { ScreenToModulePath(it, ModulePath(":app")) }.toSet()
        }
    }
}
