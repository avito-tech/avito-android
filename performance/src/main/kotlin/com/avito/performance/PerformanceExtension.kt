package com.avito.performance

open class PerformanceExtension {

    /* Результаты перформанс-тестов, резултаты сравнения с предыдущим прогоном на девелопе */
    lateinit var output: String

    /* Название файла с результатами теста */
    lateinit var performanceTestResultName: String

    lateinit var statsUrl: String

    lateinit var slackHookUrl: String
}
