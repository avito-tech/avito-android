package com.avito.instrumentation.suite.dex

import com.avito.instrumentation.suite.dex.check.TestSignatureCheck
import com.avito.report.model.TestName
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import java.io.File

/**
 * Определяем какие тесты должны быть запущены последующим таском-раннером
 * Помимо разнообразных фильтров мы еще должны убедиться, что запрашиваемые тесты вообще находятся в тестовой apk,
 * для этого парсим dex, при помощи [TestSuiteLoader]
 */
interface TestSuiteLoader {
    fun loadTestSuite(file: File, testSignatureCheck: TestSignatureCheck? = null): List<TestInApk>
}

/**
 * @param utilityAnnotations аннотации, которые не должны попадать в classAnnotations, methodAnnotations
 *
 * todo cache map<file(string path), list<TestInApk>>
 */
class TestSuiteLoaderImpl(
    private val dexExtractor: DexFileExtractor = ApkDexFileExtractor(),
    private val annotationExtractor: AnnotationExtractor = AnnotationExtractor,
    private val utilityAnnotations: Array<String> = arrayOf(
        TEST_ANNOTATION,
        KOTLIN_METADATA_ANNOTATION
    )
) : TestSuiteLoader {

    override fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck?
    ): List<TestInApk> {

        return dexExtractor
            .getDexFiles(file)
            .flatMap { dexFile -> dexFile.classes }
            .filter { classDef -> classDef.hasTestMethods() && !classDef.isAbstract() }
            .flatMap { classDef ->

                val classAnnotations = classDef.annotations
                    .filterUtilityAnnotations()
                    .map { annotationExtractor.toAnnotationData(it) }

                classDef.methods
                    .filter { method -> method.hasTestAnnotation() }
                    .map { method ->

                        val methodAnnotations = method.annotations
                            .filterUtilityAnnotations()
                            .map { annotationExtractor.toAnnotationData(it) }

                        testSignatureCheck?.onNewMethodFound(
                            classDef.type,
                            method.name,
                            classAnnotations,
                            methodAnnotations
                        )

                        TestInApk(
                            testName = TestName(
                                className = method.definingClass.toJavaType(),
                                methodName = method.name
                            ),
                            annotations = classAnnotations + methodAnnotations
                        )
                    }
            }
    }

    private fun Set<Annotation>.filterUtilityAnnotations(): List<Annotation> =
        filter { annotation -> utilityAnnotations.none { annotation.type.contains(it) } }

    private fun ClassDef.isAbstract() =
        (this.accessFlags.and(ACC_ABSTRACT) != 0)

    private fun ClassDef.hasTestMethods() =
        methods.find { it.hasTestAnnotation() } != null

    private fun Method.hasTestAnnotation() =
        annotationExtractor.hasAnnotation(this, AnnotationType(TEST_ANNOTATION))

    private fun String.toJavaType() = if (startsWith(DEX_OBJECT_TYPE_PREFIX) && endsWith(';')) {
        substring(1, length - 1).replace('/', '.')
    } else {
        throw RuntimeException("Invalid dex object type")
    }
}

private const val DEX_OBJECT_TYPE_PREFIX = 'L'
private const val TEST_ANNOTATION = "Lorg/junit/Test;"
private const val KOTLIN_METADATA_ANNOTATION = "Lkotlin/Metadata;"

private const val ACC_ABSTRACT = 0x0400
