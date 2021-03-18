package com.avito.module.dependencies

import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

// TODO: try to get rid of the printing task. It's used for testing only and fragile to changed output
//  We can cover all cases by unit tests easier (see DependenciesGraphTest)
internal class AppsDependenciesGraphTest {

    lateinit var projectDir: File

    @BeforeEach
    fun before(@TempDir dir: File) {
        projectDir = dir
        DependenciesGraphTestProject.generate(projectDir)
    }

    @Test
    fun `print android_test dependencies graph`() {
        gradlew(projectDir, "printAppsDependenciesGraph", "--configuration=android_test")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
                |Configuration type: ConfigurationType.AndroidTests
                |Root Node :CopyRootB
                |:CopyRootB depends on :LeafG in ConfigurationType.AndroidTests
                |:CopyRootB depends on :UniqueCopyB in ConfigurationType.AndroidTests
                |:CopyRootB Node :LeafG
                |:LeafG depends on :LeafE in ConfigurationType.Main
                |:LeafG Node :LeafE
                |:CopyRootB Node :UniqueCopyB
                |Root Node :RootA
                |:RootA depends on :LeafE in ConfigurationType.AndroidTests
                |:RootA depends on :UniqueA in ConfigurationType.AndroidTests
                |:RootA Node :LeafE
                |:RootA Node :UniqueA
                |Root Node :RootB
                |:RootB depends on :LeafG in ConfigurationType.AndroidTests
                |:RootB depends on :UniqueB in ConfigurationType.AndroidTests
                |:RootB Node :LeafG
                |:LeafG depends on :LeafE in ConfigurationType.Main
                |:LeafG Node :LeafE
                |:RootB Node :UniqueB
                |Configuration type: ConfigurationType.Main
                |Root Node :CopyRootB
                |:CopyRootB depends on :LeafC in ConfigurationType.Main
                |:CopyRootB Node :LeafC
                |:LeafC depends on :LeafD in ConfigurationType.Main
                |:LeafC Node :LeafD
                |:LeafD depends on :LeafE in ConfigurationType.Main
                |:LeafD Node :LeafE
                |Root Node :RootA
                |:RootA depends on :LeafF in ConfigurationType.Main
                |:RootA depends on :LeafE in ConfigurationType.Main
                |:RootA Node :LeafF
                |:RootA Node :LeafE
                |Root Node :RootB
                |:RootB depends on :LeafC in ConfigurationType.Main
                |:RootB Node :LeafC
                |:LeafC depends on :LeafD in ConfigurationType.Main
                |:LeafC Node :LeafD
                |:LeafD depends on :LeafE in ConfigurationType.Main
                |:LeafD Node :LeafE
                |print ended""".trimMargin()
            )
    }

    @Test
    fun `print main dependencies graph`() {
        gradlew(projectDir, "printAppsDependenciesGraph", "--configuration=main")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
                |> Task :printAppsDependenciesGraph
                |Configuration type: ConfigurationType.Main
                |Root Node :CopyRootB
                |:CopyRootB depends on :LeafC in ConfigurationType.Main
                |:CopyRootB Node :LeafC
                |:LeafC depends on :LeafD in ConfigurationType.Main
                |:LeafC Node :LeafD
                |:LeafD depends on :LeafE in ConfigurationType.Main
                |:LeafD Node :LeafE
                |Root Node :RootA
                |:RootA depends on :LeafF in ConfigurationType.Main
                |:RootA depends on :LeafE in ConfigurationType.Main
                |:RootA Node :LeafF
                |:RootA Node :LeafE
                |Root Node :RootB
                |:RootB depends on :LeafC in ConfigurationType.Main
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
        gradlew(projectDir, "printAppsDependenciesGraph", "--flatten", "--configuration=android_test")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
                |> Task :printAppsDependenciesGraph
                |Configuration type: ConfigurationType.AndroidTests
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
                |Configuration type: ConfigurationType.Main
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

    @Test
    fun `print main flatten dependencies graph`() {
        gradlew(projectDir, "printAppsDependenciesGraph", "--flatten", "--configuration=main")
            .assertThat()
            .buildSuccessful()
            .outputContains(
                """
                |> Task :printAppsDependenciesGraph
                |Configuration type: ConfigurationType.Main
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
