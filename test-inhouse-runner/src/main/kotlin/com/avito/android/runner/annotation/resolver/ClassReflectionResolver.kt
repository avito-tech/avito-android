package com.avito.android.runner.annotation.resolver

/**
 * Просто достаем по имени теста объект Class<*>
 * Наследники должны при помощи [resolver] определить значение [key] в тестовом bundle
 */
open class ClassReflectionResolver(
    override val key: String,
    internal val resolver: (Class<*>) -> TestMetadataResolver.Resolution
) : TestMetadataResolver {

    override fun resolve(test: String): TestMetadataResolver.Resolution =
        when (val parseResolution = MethodStringRepresentation.parseString(test)) {

            is MethodStringRepresentation.Resolution.ClassOnly ->
                resolver(parseResolution.aClass)

            is MethodStringRepresentation.Resolution.Method ->
                resolver(parseResolution.aClass)

            is MethodStringRepresentation.Resolution.ParseError ->
                TestMetadataResolver.Resolution.NothingToChange(parseResolution.message)
        }
}
