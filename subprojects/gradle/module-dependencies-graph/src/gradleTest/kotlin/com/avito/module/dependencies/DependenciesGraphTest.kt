package com.avito.module.dependencies

import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class DependenciesGraphTest {

    lateinit var projectDir: File

    @BeforeEach
    fun before(@TempDir dir: File) {
        projectDir = dir
        DependenciesGraphTestProject.generate(projectDir)
    }

    @Test
    fun `print dependencies graph`() {
        gradlew(projectDir, "printDependenciesGraph")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
                |> Task :printDependenciesGraph
                |Root Node :CopyRootB
                |:CopyRootB depends on :LeafG in ConfigurationType.AndroidTests
                |:CopyRootB depends on :UniqueCopyB in ConfigurationType.AndroidTests
                |:CopyRootB depends on :LeafC in ConfigurationType.Main
                |:CopyRootB Node :LeafG
                |:LeafG depends on :LeafE in ConfigurationType.Main
                |:LeafG Node :LeafE
                |:CopyRootB Node :UniqueCopyB
                |:CopyRootB Node :LeafC
                |:LeafC depends on :LeafD in ConfigurationType.Main
                |:LeafC Node :LeafD
                |:LeafD depends on :LeafE in ConfigurationType.Main
                |:LeafD Node :LeafE
                |Root Node :RootA
                |:RootA depends on :LeafE in ConfigurationType.AndroidTests
                |:RootA depends on :UniqueA in ConfigurationType.AndroidTests
                |:RootA depends on :LeafF in ConfigurationType.Main
                |:RootA depends on :LeafE in ConfigurationType.Main
                |:RootA Node :LeafE
                |:RootA Node :UniqueA
                |:RootA Node :LeafF
                |Root Node :RootB
                |:RootB depends on :LeafG in ConfigurationType.AndroidTests
                |:RootB depends on :UniqueB in ConfigurationType.AndroidTests
                |:RootB depends on :LeafC in ConfigurationType.Main
                |:RootB Node :LeafG
                |:LeafG depends on :LeafE in ConfigurationType.Main
                |:LeafG Node :LeafE
                |:RootB Node :UniqueB
                |:RootB Node :LeafC
                |:LeafC depends on :LeafD in ConfigurationType.Main
                |:LeafC Node :LeafD
                |:LeafD depends on :LeafE in ConfigurationType.Main
                |:LeafD Node :LeafE
                |print ended""".trimMargin()
            )
    }

    @Test
    fun `print android_test flatten dependencies graph`() {
        gradlew(projectDir, "printDependenciesGraph", "--flatten")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
                |> Task :printDependenciesGraph
                |Root :CopyRootB
                |:CopyRootB graph contains :LeafG
                |:CopyRootB graph contains :LeafE
                |:CopyRootB graph contains :UniqueCopyB
                |Root :RootA
                |:RootA graph contains :LeafE
                |:RootA graph contains :UniqueA
                |Root :RootB
                |:RootB graph contains :LeafG
                |:RootB graph contains :LeafE
                |:RootB graph contains :UniqueB
                |print ended""".trimMargin()
            )
    }

    @Test
    fun `print main flatten dependencies graph`() {
        gradlew(projectDir, "printDependenciesGraph", "--flatten", "--configuration=main")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
                |> Task :printDependenciesGraph
                |Root :CopyRootB
                |:CopyRootB graph contains :LeafC
                |:CopyRootB graph contains :LeafD
                |:CopyRootB graph contains :LeafE
                |Root :RootA
                |:RootA graph contains :LeafF
                |:RootA graph contains :LeafE
                |Root :RootB
                |:RootB graph contains :LeafC
                |:RootB graph contains :LeafD
                |:RootB graph contains :LeafE
                |print ended""".trimMargin()
            )
    }
}
