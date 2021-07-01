package com.avito.module.dependencies

import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FindAndroidAppTaskNegativeScenariosTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun before(@TempDir dir: File) {
        projectDir = dir
        DependenciesGraphTestProject.generate(projectDir)
    }

    @Test
    fun `no modules - fail`() {
        gradlew(projectDir, "findAndroidApp", expectFailure = true)
            .assertThat()
            .outputContains("property 'modules' doesn't have a configured value")
    }

    @Test
    fun `incorrect module name - fail`() {
        gradlew(projectDir, "findAndroidApp", "--modules=stub", expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("module 'stub' must contain : and be absolute")
    }

    @Test
    fun `not existed module - fail`() {
        gradlew(projectDir, "findAndroidApp", "--modules=:stub", expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("module ':stub' does not exist")
    }
}
