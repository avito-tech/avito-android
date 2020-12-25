package com.avito.android.runner.annotation.resolver

/**
 * Просто достаем по имени теста объект Class<*>
 * Наследники должны при помощи [resolver] определить значение [key] в тестовом bundle
 */
open class ClassReflectionResolver(
    override val key: String,
    internal val resolver: (Class<*>) -> TestMetadataResolver.Resolution
) : TestMetadataResolver {

    override fun resolve(test: TestMethodOrClass): TestMetadataResolver.Resolution = resolver(test.testClass)
}
