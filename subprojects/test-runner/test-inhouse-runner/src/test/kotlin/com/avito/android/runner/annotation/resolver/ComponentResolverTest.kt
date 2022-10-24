package com.avito.android.runner.annotation.resolver

import com.avito.android.mock.MockWebServerApiRule
import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Rule
import org.junit.jupiter.api.Test

class ComponentResolverTest {

    @Test
    fun `resolver - resolves mock web server type - for test with MockApiRule`() {
        val file = FileSpec.builder("", "Test")
            .addType(
                TypeSpec.classBuilder("Test")
                    .addProperty(
                        PropertySpec.builder("mockApi", MockWebServerApiRule::class)
                            .initializer("MockWebServerApiRule()")
                            .addAnnotation(
                                AnnotationSpec.builder(Rule::class)
                                    .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                                    .build()
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("test")
                            .build()
                    )
                    .build()
            )
            .build()

        // TODO: explain why don't we use simple fixtures and build classes on the fly
        //  It's harder to understand and maintain.
        val aClass = compile(file)

        assertThat(NetworkingResolver().resolver(aClass)).isEqualTo(
            TestMetadataResolver.Resolution.ReplaceSerializable(
                NetworkingType.MOCK_WEB_SERVER
            )
        )
    }

    @Test
    fun `resolver - resolves mocked network layer type - for test with AbstractMockitoApiRule child`() {
        val file = FileSpec.builder("", "Test")
            .addType(
                TypeSpec.classBuilder("Test")
                    .addProperty(
                        PropertySpec.builder("mockApi", TestMockApiRule::class)
                            .initializer("TestMockApiRule()")
                            .addAnnotation(
                                AnnotationSpec.builder(Rule::class)
                                    .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                                    .build()
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("test")
                            .build()
                    )
                    .build()
            )
            .build()

        val aClass = compile(file)

        assertThat(NetworkingResolver().resolver(aClass)).isEqualTo(
            TestMetadataResolver.Resolution.ReplaceSerializable(
                NetworkingType.MOCKED_NETWORK_LAYER
            )
        )
    }

    private fun compile(file: FileSpec): Class<*> {
        val src = SourceFile.kotlin("${file.name}.kt", file.toString())

        val result = KotlinCompilation().apply {
            sources = listOf(src)
            messageOutputStream = System.out
            inheritClassPath = true
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val packageSlug = if (file.packageName.isBlank()) "" else "${file.packageName}."
        return result.classLoader.loadClass("$packageSlug${file.name}")
    }
}
