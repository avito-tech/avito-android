package com.avito.android

import com.avito.android.InstrumentationChangedTestsFinderApi.changedTestsFinderTaskName
import com.avito.android.InstrumentationChangedTestsFinderApi.pluginId
import com.avito.git.Git
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

internal class FindChangedTestsActionTest {

    private lateinit var projectDir: File
    private lateinit var androidTestDir: File
    private lateinit var testGroupFile: File
    private lateinit var anotherTestFile: File
    private lateinit var multipleClassesTestFile: File
    private lateinit var git: Git
    private lateinit var targetCommit: String

    @BeforeEach
    fun setup(@TempDir projectDir: File) {
        this.projectDir = projectDir
        git = Git.create(projectDir)

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(pluginId)
                    },
                    buildGradleExtra = """
                        changedTests {
                            targetCommit = project.properties.get("targetCommit")
                        }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        this.androidTestDir = projectDir.dir("app/src/androidTest/kotlin")

        testGroupFile = androidTestDir.file(
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

        anotherTestFile = androidTestDir.file(
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

        multipleClassesTestFile = androidTestDir.file(
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

        git.init().getOrThrow()
        git.checkout(branchName = "develop", create = true).getOrThrow()
        git.addAll().getOrThrow()
        git.commit("initial").getOrThrow()

        targetCommit = git.tryParseRev("develop").getOrThrow()
    }

    @Test
    fun `finds modified file's class`() {
        git.checkout(branchName = "changes", create = true).getOrThrow()
        mutateKotlinFile(anotherTestFile)
        git.addAll().getOrThrow()
        git.commit("some changes").getOrThrow()

        gradlew(projectDir, changedTestsFinderTaskName, "-PtargetCommit=$targetCommit")
            .assertThat()
            .buildSuccessful()

        val output = Paths.get(projectDir.path, "app", "build", "changed-test-classes.txt").toFile()

        assertThat(output.exists()).isTrue()
        assertThat(output.readText()).isEqualTo("com.avito.android.test.AnotherTest")
    }

    private fun mutateKotlinFile(file: File) {
        file.appendText("\n private fun newFun() { }")
    }

    @Test
    fun `finds added file's class`() {
        git.checkout(branchName = "addition", create = true).getOrThrow()
        androidTestDir.file(
            "NewTest.kt",
            """
            package com.avito.android.test
            
            import org.junit.Test
            
            class NewTest {
            
                @Test
                fun test() {
                }
            }
        """.trimIndent()
        )
        git.addAll().getOrThrow()
        git.commit("new test added").getOrThrow()

        gradlew(projectDir, changedTestsFinderTaskName, "-PtargetCommit=$targetCommit")
            .assertThat()
            .buildSuccessful()

        val output = Paths.get(projectDir.path, "app", "build", "changed-test-classes.txt").toFile()

        assertThat(output.exists()).isTrue()
        assertThat(output.readText()).isEqualTo("com.avito.android.test.NewTest")
    }

    @Test
    fun `finds all tests in modified file with multiple classes`() {
        git.checkout(branchName = "multiple-classes", create = true).getOrThrow()
        mutateKotlinFile(multipleClassesTestFile)
        git.addAll().getOrThrow()
        git.commit("change in test").getOrThrow()

        gradlew(projectDir, changedTestsFinderTaskName, "-PtargetCommit=$targetCommit")
            .assertThat()
            .buildSuccessful()

        val output = Paths.get(projectDir.path, "app", "build", "changed-test-classes.txt").toFile()

        assertThat(output.exists()).isTrue()
        assertThat(output.readText()).isEqualTo(
            "com.avito.android.test.TestClassInFileOne\ncom.avito.android.test.TestClassInFileTwo"
        )
    }

    @Test
    fun `finds nothing if nothing changes`() {
        gradlew(projectDir, changedTestsFinderTaskName, "-PtargetCommit=$targetCommit")
            .assertThat()
            .buildSuccessful()

        val output = Paths.get(projectDir.path, "app", "build", "changed-test-classes.txt").toFile()

        assertThat(output.exists()).isTrue()
        assertThat(output.readText()).isEmpty()
    }
}
