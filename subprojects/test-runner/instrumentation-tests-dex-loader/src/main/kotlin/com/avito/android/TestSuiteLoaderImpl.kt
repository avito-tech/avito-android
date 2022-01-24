package com.avito.android

import com.avito.android.check.TestSignatureCheck
import com.avito.android.internal.AnnotationExtractor
import com.avito.android.internal.AnnotationType
import com.avito.android.internal.ApkDexFileExtractor
import com.avito.test.model.TestName
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import java.io.File

/**
 * @param utilityAnnotations optimize by skip parsing annotations not usable in further logic
 */
internal class TestSuiteLoaderImpl(
    private val dexExtractor: DexFileExtractor = ApkDexFileExtractor(),
    private val utilityAnnotations: Array<String> = arrayOf(
        TEST_ANNOTATION,
        KOTLIN_METADATA_ANNOTATION
    )
) : TestSuiteLoader {

    private val annotationExtractor: AnnotationExtractor = AnnotationExtractor

    override fun loadTestSuite(
        file: File,
        testSignatureCheck: TestSignatureCheck?
    ): Result<List<TestInApk>> {

        return Result.tryCatch {
            val tests = dexExtractor
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

            check(tests.isNotEmpty()) {
                "No tests found in test apk: ${file.path}"
            }

            tests
        }
    }

    private fun Set<Annotation>.filterUtilityAnnotations(): List<Annotation> =
        filter { annotation -> utilityAnnotations.none { annotation.type.contains(it) } }

    private fun ClassDef.isAbstract() = this.accessFlags.and(ACC_ABSTRACT) != 0

    private fun ClassDef.hasTestMethods() = methods.find { it.hasTestAnnotation() } != null

    private fun Method.hasTestAnnotation() =
        annotationExtractor.hasAnnotation(this, AnnotationType(TEST_ANNOTATION))

    private fun String.toJavaType() = if (startsWith(DEX_OBJECT_TYPE_PREFIX) && endsWith(';')) {
        substring(1, length - 1).replace('/', '.')
    } else {
        throw IllegalStateException("Invalid dex object type")
    }

    internal companion object {
        private const val DEX_OBJECT_TYPE_PREFIX = 'L'
        private const val TEST_ANNOTATION = "Lorg/junit/Test;"
        private const val KOTLIN_METADATA_ANNOTATION = "Lkotlin/Metadata;"

        private const val ACC_ABSTRACT = 0x0400
    }
}
