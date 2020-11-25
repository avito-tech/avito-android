package com.avito.instrumentation.suite.dex

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.isInstanceOf
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.funktionale.tries.Try
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.iface.AnnotationElement
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.DexFile
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.io.File

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestSuiteLoaderTest {

    @Mock
    lateinit var dexFileExtractor: DexFileExtractor

    private lateinit var testSuiteLoader: TestSuiteLoader

    @BeforeEach
    fun setUp() {
        testSuiteLoader = TestSuiteLoaderImpl(dexFileExtractor)
    }

    @Test
    fun `load test suite - returns error - on incorrect file`() {
        val tests = testSuiteLoader.loadTestSuite(File("."))
        assertThat(tests).isInstanceOf<Try.Failure<*>>()
    }

    @Test
    fun `load test suite - returns one test case - class with test annotated method`() {
        givenClasses(
            createClass("Lru/avito/Test;") {
                withMethods(
                    createMethod("test1", createAnnotation("Lorg/junit/Test;"))
                )
                withAnnotations(
                    createCaseIdAnnotation()
                )
            }
        )

        val tests = loadTestSuite()

        assertThat(tests).hasSize(1)
        assertThat(tests.first().testName.className).isEqualTo("ru.avito.Test")
        assertThat(tests.first().testName.methodName).isEqualTo("test1")
    }

    @Test
    fun `load test suite - does not returns test cases - class contains method without annotations`() {
        givenClasses(
            createClass("Lru/avito/Test;") {
                withMethods(
                    createMethod("test1")
                )
                withAnnotations(
                    createCaseIdAnnotation()
                )
            }
        )

        val tests = testSuiteLoader.loadTestSuite(mock())

        assertThat(tests).isInstanceOf<Try.Failure<*>>()
    }

    @Test
    fun `load test suite - returns all test cases - multiple classes with test annotated methods`() {
        givenClasses(
            createClass("Lru/avito/Test1;") {
                withMethods(
                    createMethod("test1", createAnnotation("Lorg/junit/Test;")),
                    createMethod("test2", createAnnotation("Lorg/junit/Test;"))
                )
                withAnnotations(
                    createCaseIdAnnotation()
                )
            },
            createClass("Lru/avito/Test2;") {
                withMethods(
                    createMethod("test1", createAnnotation("Lorg/junit/Test;")),
                    createMethod("test2", createAnnotation("Lorg/junit/Test;"))
                )
                withAnnotations(
                    createCaseIdAnnotation()
                )
            }
        )

        val actualTests = loadTestSuite()

        assertThat(actualTests).hasSize(4)
        actualTests.zip(
            listOf(
                TestInApk.createStubInstance(className = "ru.avito.Test1", methodName = "test1"),
                TestInApk.createStubInstance(className = "ru.avito.Test1", methodName = "test2"),
                TestInApk.createStubInstance(className = "ru.avito.Test2", methodName = "test1"),
                TestInApk.createStubInstance(className = "ru.avito.Test2", methodName = "test2")
            )
        ).forEach { (actual, expected) ->
            assertThat(actual.testName.className).isEqualTo(expected.testName.className)
            assertThat(actual.testName.methodName).isEqualTo(expected.testName.methodName)
        }
    }

    @Test
    fun `load test suite - returns test cases with class annotation - class with custom annotation`() {
        givenClasses(
            createClass("Lru/avito/Test;") {
                withAnnotations(
                    createAnnotation("Lcom/avito/android/FunctionalTest;"),
                    createCaseIdAnnotation()
                )
                withMethods(
                    createMethod(
                        "test1",
                        createAnnotation("Lorg/junit/Test;")
                    )
                )
            }
        )

        val tests = loadTestSuite()

        assertThat(tests).hasSize(1)
        assertThat(tests.first().testName.className).isEqualTo("ru.avito.Test")
        assertThat(tests.first().testName.methodName).isEqualTo("test1")
        assertThat(tests.first().annotations).containsExactly(
            AnnotationData(
                name = "com.avito.android.FunctionalTest",
                values = emptyMap()
            ),
            AnnotationData(
                name = "com.avito.CaseId",
                values = mapOf("value" to 0)
            )
        )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `load test suite - returns test cases with class annotation - multiple methods in class with custom annotation`() {
        givenClasses(
            createClass("Lru/avito/Test;") {
                withAnnotations(
                    createAnnotation("Lcom/avito/android/FunctionalTest;"),
                    createCaseIdAnnotation()
                )
                withMethods(
                    createMethod(
                        "test1",
                        createAnnotation("Lorg/junit/Test;")
                    ),
                    createMethod(
                        "test2",
                        createAnnotation("Lorg/junit/Test;")
                    ),
                    createMethod(
                        "test3",
                        createAnnotation("Lorg/junit/Test;")
                    )
                )
            }
        )

        val tests = loadTestSuite()

        assertThat(tests).containsAtLeast(
            TestInApk.createStubInstance(
                className = "ru.avito.Test",
                methodName = "test1",
                annotations = listOf(
                    AnnotationData("com.avito.android.FunctionalTest", emptyMap()),
                    AnnotationData("com.avito.CaseId", mapOf("value" to 0))
                )
            ),
            TestInApk.createStubInstance(
                className = "ru.avito.Test",
                methodName = "test2",
                annotations = listOf(
                    AnnotationData("com.avito.android.FunctionalTest", emptyMap()),
                    AnnotationData("com.avito.CaseId", mapOf("value" to 0))
                )
            ),
            TestInApk.createStubInstance(
                className = "ru.avito.Test",
                methodName = "test3",
                annotations = listOf(
                    AnnotationData("com.avito.android.FunctionalTest", emptyMap()),
                    AnnotationData("com.avito.CaseId", mapOf("value" to 0))
                )
            )
        )
    }

    @Test
    fun `load tests - returns test case - method with annotation`() {
        givenClasses(
            createClass("Lru/avito/Test;") {
                withAnnotations(createCaseIdAnnotation())
                withMethods(
                    createMethod(
                        "test1",
                        createAnnotation("Lcom/avito/android/FunctionalTest;"),
                        createAnnotation("Lorg/junit/Test;")
                    )
                )
            }
        )

        val tests = loadTestSuite()

        assertThat(tests).contains(
            TestInApk.createStubInstance(
                className = "ru.avito.Test",
                methodName = "test1",
                annotations = listOf(
                    AnnotationData("com.avito.CaseId", mapOf("value" to 0)),
                    AnnotationData("com.avito.android.FunctionalTest", emptyMap())
                )
            )
        )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `load tests - returns test case - class with annotation and include annotation filter contains this + other annotation`() {
        givenClasses(
            createClass("Lru/avito/Test;") {
                withAnnotations(
                    createCaseIdAnnotation(),
                    createAnnotation("Lcom/avito/android/FunctionalTest;")
                )
                withMethods(
                    createMethod(
                        "test1",
                        createAnnotation("Lorg/junit/Test;")
                    )
                )
            }
        )

        val tests = loadTestSuite()

        assertThat(tests).contains(
            TestInApk.createStubInstance(
                className = "ru.avito.Test",
                methodName = "test1",
                annotations = listOf(
                    AnnotationData("com.avito.CaseId", mapOf("value" to 0)),
                    AnnotationData("com.avito.android.FunctionalTest", emptyMap())
                )
            )
        )
    }

    private fun givenClasses(vararg classes: ClassDef) {
        val dexFile = mock<DexFile>()
        whenever(dexFile.classes).thenReturn(classes.toSet())
        whenever(dexFileExtractor.getDexFiles(any())).thenReturn(listOf(dexFile))
    }

    private fun loadTestSuite(): List<TestInApk> {
        return testSuiteLoader.loadTestSuite(mock()).get()
    }

    private fun createClass(type: String, block: ClassDef.() -> Unit = {}): ClassDef {
        val classDef = mock<ClassDef>()
        whenever(classDef.type).thenReturn(type)
        classDef.block()
        return classDef
    }

    private fun ClassDef.withMethods(vararg methods: Method) {
        whenever(this.methods).thenReturn(methods.toList())
        methods.forEach {
            val type = this.type
            whenever(it.definingClass).thenReturn(type)
        }
    }

    private fun ClassDef.withAnnotations(vararg annotations: Annotation) {
        whenever(this.annotations).thenReturn(annotations.toSet())
    }

    private fun createMethod(name: String, vararg annotations: Annotation): Method {
        val method = mock<Method>()
        whenever(method.name).thenReturn(name)
        whenever(method.annotations).thenReturn(annotations.toSet())
        return method
    }

    private fun createAnnotation(
        type: String
    ) = mock<Annotation>().apply {
        whenever(this.type).thenReturn(type)
    }

    private fun createCaseIdAnnotation(
        value: Int = 0
    ) = mock<Annotation>().apply {
        whenever(this.type).thenReturn("Lcom.avito.CaseId;")
        val element: AnnotationElement = mock()

        whenever(element.name).thenReturn("value")
        whenever(element.value).thenReturn(ImmutableIntEncodedValue(value))

        whenever(this.elements).thenReturn(setOf(element))
    }
}
