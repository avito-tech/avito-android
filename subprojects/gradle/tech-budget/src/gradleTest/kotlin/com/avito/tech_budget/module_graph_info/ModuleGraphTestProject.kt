package com.avito.tech_budget.module_graph_info

import com.avito.android.utils.FAKE_OWNERSHIP_EXTENSION
import com.avito.tech_budget.utils.dumpInfoExtension
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.plugin.plugins
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import java.io.File

internal fun generateModuleGraphTestProject(
    projectDir: File,
    mockWebServer: MockWebServer,
) =
    TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.gradle-logger")
            id("com.avito.android.code-ownership")
            id("com.avito.android.tech-budget")
            id("com.avito.android.tls-configuration")
        },
        useKts = true,
        imports = listOf(
            "import org.gradle.api.DefaultTask",
            "import com.avito.android.module_graph.GenerateModuleGraphTask",
            "import com.avito.android.module_graph.ModuleGraphTask",
            "import org.gradle.api.tasks.TaskAction",
            "import java.io.File"
        ),
        buildGradleExtra = """
                $FAKE_OWNERSHIP_EXTENSION
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                    abstract class GenerateTask : DefaultTask(), ModuleGraphTask {
                            
                            @get:OutputFile
                            abstract override val outputFile: RegularFileProperty
                    }
                        project.tasks.register<GenerateTask>("generateModuleGraph") {
                            outputFile.set(project.file("module-graph.json"))
                        }
                }
            """.trimIndent(),
        modules = listOf(),
    ).generateIn(projectDir).also {
        projectDir.file("module-graph.json", MODULE_GRAPH_INFO_JSON)
    }

@Language("json")
internal val MODULE_GRAPH_INFO_JSON = """
            {
               "dependencies": [
                  {
                       "from": ":demo-app-1",
                       "to": ":module-1",
                       "type": "Implementation"
                   },
                   {
                       "from": ":demo-app-2",
                       "to": ":module-2",
                       "type": "Implementation"
                   },
                   {
                       "from": ":module-1",
                       "to": ":module-2",
                       "type": "Implementation"
                   }
               ],
               "sizes": {
                    ":demo-app-1": 30,
                    ":demo-app-2": 20,
                    ":module-1": 10,
                    ":module-2": 10
               },
               "modulesToDemoApps": {
                    ":module-1": [
                      ":demo-app-1"
                    ],
                    ":module-2": [
                      ":demo-app-1", ":demo-app-2"
                    ]
               }
            }    
        """.trimIndent()
