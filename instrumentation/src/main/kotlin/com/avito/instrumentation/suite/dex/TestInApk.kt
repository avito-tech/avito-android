package com.avito.instrumentation.suite.dex

import com.avito.report.model.TestName

/**
 * Тест это обязательно метод, запуск на классе хоть и поддерживается из IDE или фильтром по классу,
 * по факту обозначает запуск всех методов в классе
 *
 * @param annotations при совпадении аннотаций на классе и методе будет выброшен exception
 */
data class TestInApk(
    val testName: TestName,
    val annotations: List<AnnotationData>
) {
    companion object
}
