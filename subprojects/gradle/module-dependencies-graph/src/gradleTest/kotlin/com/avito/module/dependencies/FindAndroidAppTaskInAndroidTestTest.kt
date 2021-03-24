package com.avito.module.dependencies

import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test

internal class FindAndroidAppTaskInAndroidTestTest : BaseFindAndroidAppTaskTest() {

    @Test
    override fun `find one suitable app - advice this app`() {
        gradlew(projectDir, "findAndroidApp", "--modules=:UniqueA", "--configuration=android_test")
            .assertThat()
            .buildSuccessful()
            .outputContains("In your project is only one suitable app :RootA")
    }

    @Test
    override fun `find multiple same suitable apps - advice that you could choose both`() {
        gradlew(projectDir, "findAndroidApp", "--modules=:LeafG", "--configuration=android_test")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
            |There are multiple suitable apps [:CopyRootB, :RootB]
            |You should prefer [:CopyRootB, :RootB] because they have the least dependencies in graph size=5
            """.trimMargin()
            )
    }

    @Test
    override fun `find multiple suitable apps - advice with that has the least dependencies`() {
        gradlew(projectDir, "findAndroidApp", "--modules=:LeafE", "--configuration=android_test")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
            |There are multiple suitable apps [:CopyRootB, :RootA, :RootB]
            |You should prefer :RootA because it has the least dependencies in graph size=3
            """.trimMargin()
            )
    }

    @Test
    override fun `don't find any suitable app - advice that there are no apps`() {
        gradlew(projectDir, "findAndroidApp", "--modules=:UniqueA,:UniqueB", "--configuration=android_test")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
                |There are no suitable Android apps
                |Apps are sorted by missing modules:
                |App :RootA contains [:UniqueA] but missed [:UniqueB]
                |App :RootB contains [:UniqueB] but missed [:UniqueA]
                |App :CopyRootB contains [] but missed [:UniqueA, :UniqueB]
            """.trimMargin()
            )
    }
}
