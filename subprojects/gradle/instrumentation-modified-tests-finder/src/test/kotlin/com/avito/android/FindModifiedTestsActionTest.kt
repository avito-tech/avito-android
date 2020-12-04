package com.avito.android

import com.avito.impact.changes.ChangeType
import com.avito.impact.changes.ChangedFile
import com.avito.impact.changes.FakeChangesDetector
import com.avito.instrumentation.impact.KotlinClassesFinderImpl
import com.avito.report.model.TestName
import com.avito.test.gradle.file
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.isInstanceOf
import org.funktionale.tries.Try
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FindModifiedTestsActionTest {

    private val changesDetector = FakeChangesDetector()
    private val kotlinClassFinder = KotlinClassesFinderImpl()
    private val action = FindModifiedTestsAction(changesDetector, kotlinClassFinder)

    private lateinit var dir: File

    private lateinit var testGroupFile: File
    private lateinit var anotherTestFile: File
    private lateinit var multipleClassesTestFile: File

    private val allTestsInApk = listOf(
        TestName(className = "com.avito.android.test.TestGroup", methodName = "testOne"),
        TestName(className = "com.avito.android.test.TestGroup", methodName = "testTwo"),
        TestName(className = "com.avito.android.test.AnotherTest", methodName = "test"),
        TestName(className = "com.avito.android.test.TestClassInFileOne", methodName = "test"),
        TestName(className = "com.avito.android.test.TestClassInFileTwo", methodName = "test")
    )

    @BeforeEach
    fun setup(@TempDir dir: File) {
        this.dir = dir

        testGroupFile = dir.file(
            "TestGroup.kt",
            """
            package com.avito.android.test
            
            import org.junit.Test
            
            class TestGroup {
            
                @Test
                fun testOne() {
                }
                
                @Test
                fun testTwo() {
                }
            }
        """.trimIndent()
        )

        anotherTestFile = dir.file(
            "AnotherTest.kt",
            """
            package com.avito.android.test
            
            import org.junit.Test
            
            class AnotherTest {
            
                @Test
                fun test() {
                }
            }
        """.trimIndent()
        )

        multipleClassesTestFile = dir.file(
            "YetAnotherTest.kt",
            """
            package com.avito.android.test
            
            import org.junit.Test
            
            class TestClassInFileOne {
            
                @Test
                fun test() {
                }
            }
            
            class TestClassInFileTwo {
            
                @Test
                fun test() {
                }
            }
        """.trimIndent()
        )
    }

    @Test
    fun `finds modified file's class`() {
        changesDetector.result = Try.Success(
            listOf(
                ChangedFile(dir, anotherTestFile, ChangeType.MODIFIED)
            )
        )

        val result = action.find(
            androidTestDir = dir,
            allTestsInApk = allTestsInApk
        )

        assertThat(result).isInstanceOf<Try.Success<*>>()
        assertThat(result.get()).containsExactly("com.avito.android.test.AnotherTest.test")
    }

    @Test
    fun `finds added file's class`() {
        changesDetector.result = Try.Success(
            listOf(
                ChangedFile(dir, anotherTestFile, ChangeType.ADDED)
            )
        )

        val result = action.find(
            androidTestDir = dir,
            allTestsInApk = allTestsInApk
        )

        assertThat(result).isInstanceOf<Try.Success<*>>()
        assertThat(result.get()).containsExactly("com.avito.android.test.AnotherTest.test")
    }

    @Test
    fun `finds all tests in modified class`() {
        changesDetector.result = Try.Success(
            listOf(
                ChangedFile(dir, testGroupFile, ChangeType.MODIFIED)
            )
        )

        val result = action.find(
            androidTestDir = dir,
            allTestsInApk = allTestsInApk
        )

        assertThat(result).isInstanceOf<Try.Success<*>>()
        assertThat(result.get()).containsExactly(
            "com.avito.android.test.TestGroup.testOne",
            "com.avito.android.test.TestGroup.testTwo"
        )
    }

    @Test
    fun `finds all tests in modified file with multiple classes`() {
        changesDetector.result = Try.Success(
            listOf(
                ChangedFile(dir, multipleClassesTestFile, ChangeType.MODIFIED)
            )
        )

        val result = action.find(
            androidTestDir = dir,
            allTestsInApk = allTestsInApk
        )

        assertThat(result).isInstanceOf<Try.Success<*>>()
        assertThat(result.get()).containsExactly(
            "com.avito.android.test.TestClassInFileOne.test",
            "com.avito.android.test.TestClassInFileTwo.test"
        )
    }

    @Test
    fun `finds nothing is nothing changes`() {
        changesDetector.result = Try.Success(emptyList())

        val result = action.find(
            androidTestDir = dir,
            allTestsInApk = allTestsInApk
        )

        assertThat(result).isInstanceOf<Try.Success<*>>()
        assertThat(result.get()).isEmpty()
    }

    @Test
    fun `find fails if changed detector fails`() {
        changesDetector.result = Try.Failure(IllegalStateException("Something went wrong"))

        val result = action.find(
            androidTestDir = dir,
            allTestsInApk = allTestsInApk
        )

        assertThat(result).isInstanceOf<Try.Failure<*>>()
    }
}
