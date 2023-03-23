package com.avito.tech_budget.module_dependencies

import com.avito.tech_budget.utils.successResponse
import com.avito.test.gradle.gradlew
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModuleDependenciesIntegrationTest {

    @field:TempDir
    private lateinit var projectDir: File

    private val mockDispatcher = MockDispatcher(unmockedResponse = successResponse())
    private val mockWebServer = MockWebServerFactory.create()
        .apply {
            dispatcher = mockDispatcher
        }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun test() {
        ModuleDependenciesTestProject.generate(projectDir, mockWebServer.url("/").toString())
        val request = mockDispatcher.captureRequest { path.contains("dumpModuleDependencies") }

        val build = gradlew(projectDir, ":uploadModuleDependencies", "-Pcom.avito.android.tech-budget.enable=true")
        build.assertThat().buildSuccessful()

        request.checks.singleRequestCaptured()
            .bodyContains(
                """{"moduleName":":AppRootA","directImportedModulesCount":2,"directDependentModulesCount":0}"""
            )
            .bodyContains(
                """{"moduleName":":NodeE","directImportedModulesCount":0,"directDependentModulesCount":1}"""
            )
            .bodyContains(
                """{"moduleName":":NodeF","directImportedModulesCount":1,"directDependentModulesCount":2}"""
            )
            .bodyContains(
                """{"moduleName":":LeafK","directImportedModulesCount":0,"directDependentModulesCount":1}"""
            )
            .bodyContains(
                """{"moduleName":":AppRootB","directImportedModulesCount":2,"directDependentModulesCount":0}"""
            )
            .bodyContains(
                """{"moduleName":":NodeG","directImportedModulesCount":1,"directDependentModulesCount":1}"""
            )
            .bodyContains(
                """{"moduleName":":LeafM","directImportedModulesCount":0,"directDependentModulesCount":1}"""
            )
            // these are unreachable from app modules
            .bodyDoesNotContain("LibRootC")
            .bodyDoesNotContain("NodeD")
            .bodyDoesNotContain("LeafH")
            .bodyDoesNotContain("LeafJ")
            .bodyDoesNotContain("LeafL")
    }
}
