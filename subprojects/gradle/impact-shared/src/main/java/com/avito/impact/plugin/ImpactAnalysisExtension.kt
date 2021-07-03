package com.avito.impact.plugin

public open class ImpactAnalysisExtension {

    /**
     * Пропустит импакт анализ при запуске на этом списке веток
     *
     * используется ANT-style, см. https://github.com/azagniotov/ant-style-path-matcher
     */
    public var protectedBranches: Set<String> = emptySet()

    /**
     * Позволяет запускать задачи списка модулей вне зависимости от результата импакт анализа
     */
    public var excludedModules: Set<String> = emptySet()

    /**
     * Запустит все таски не учитывая impact
     */
    public var skipAnalysis: Boolean = false
}
