package com.avito.ci

import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CiStepsDynamicTest {

    @Test
    fun `cd plugin - custom task created`(@TempDir projectDir: File) {
        projectDir.file(
            name = "build.gradle",
            content = """
            plugins {
                id("com.avito.android.cd")
            }
            
            builds {
                myCustomTask {
                    description.set("My customTask description")
                }
            }
        """.trimIndent()
        )

        gradlew(projectDir, "tasks", "-Pci=true").assertThat()
            .buildSuccessful()
            .outputContains("myCustomTask - My customTask description")
    }
}
