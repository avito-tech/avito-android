package com.avito.impact.plugin

open class ImpactAnalysisExtension {

    /**
     * Пропустит импакт анализ при запуске на этом списке веток
     *
     * используется ANT-style, см. https://github.com/azagniotov/ant-style-path-matcher
     */
    var protectedBranches: Set<String> = emptySet()

    /**
     * Позволяет запускать задачи списка модулей вне зависимости от результата импакт анализа
     */
    var excludedModules: Set<String> = emptySet()

    /**
     * Запустит все таски не учитывая impact
     */
    var skipAnalysis: Boolean = false
}
