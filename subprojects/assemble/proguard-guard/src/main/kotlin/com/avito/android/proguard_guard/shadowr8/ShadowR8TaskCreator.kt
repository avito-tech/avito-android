package com.avito.android.proguard_guard.shadowr8

import com.android.build.gradle.internal.component.ConsumableCreationConfig
import com.android.build.gradle.internal.tasks.R8Task
import com.avito.capitalize
import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.tasks.TaskProvider
import java.io.File

public class ShadowR8TaskCreator(creationConfig: ConsumableCreationConfig) {
    public val name: String = computeTaskName(creationConfig)

    private val r8CreationAction = R8Task.CreationAction(
        creationConfig = creationConfig,
        isTestApplication = false,
        addCompileRClass = false
    )

    public fun registerIn(project: Project): TaskProvider<R8Task> {
        val taskProvider = project.tasks.register(name, R8Task::class.java)
        handleProvider(taskProvider)
        taskProvider.configure {
            configure(it)
        }
        return taskProvider
    }

    private fun handleProvider(taskProvider: TaskProvider<R8Task>) {
        r8CreationAction.handleProvider(taskProvider)
    }

    private fun configure(task: R8Task) {
        r8CreationAction.configure(task)

        task.classes.setFrom()
        task.resources.setFrom()

        task.shadowOutputs()
    }

    private fun computeTaskName(creationConfig: ConsumableCreationConfig): String {
        val variantName = creationConfig.name
        return "shadowedMinify${variantName.capitalize()}WithR8"
    }

    // We don't want to overwrite outputs of original R8 task
    private fun R8Task.shadowOutputs() {
        outputClasses.insertDirIntoPath(
            newDirName = "proguard_guard",
            insertAfterDir = "intermediates"
        )
        outputDex.insertDirIntoPath(
            newDirName = "proguard_guard",
            insertAfterDir = "intermediates"
        )
        projectOutputKeepRules.insertDirIntoPath(
            newDirName = "proguard_guard",
            insertAfterDir = "intermediates"
        )
        baseDexDir.insertDirIntoPath(
            newDirName = "proguard_guard",
            insertAfterDir = "intermediates"
        )
        featureDexDir.insertDirIntoPath(
            newDirName = "proguard_guard",
            insertAfterDir = "intermediates"
        )
        featureJavaResourceOutputDir.insertDirIntoPath(
            newDirName = "proguard_guard",
            insertAfterDir = "intermediates"
        )
        outputResources.insertDirIntoPath(
            newDirName = "proguard_guard",
            insertAfterDir = "intermediates"
        )
        mappingFile.insertDirIntoPath(
            newDirName = "proguard_guard",
            insertAfterDir = "outputs"
        )
    }

    /**
     * Modify location from `/a/[insertAfterDir]/b` to `/a/[insertAfterDir]/[newDirName]/b`
     */
    private fun <T : FileSystemLocation> FileSystemLocationProperty<T>.insertDirIntoPath(
        newDirName: String,
        insertAfterDir: String
    ) {
        modifyPathIfNotNull { oldPath ->
            val splittedPath = oldPath.split(File.separatorChar).toMutableList()
            val insertAfterIndex = splittedPath.indexOf(insertAfterDir)
            if (insertAfterIndex == -1) {
                return
            }
            splittedPath.add(insertAfterIndex + 1, newDirName)

            splittedPath.joinToString(separator = File.separator)
        }
    }

    private inline fun <T : FileSystemLocation> FileSystemLocationProperty<T>.modifyPathIfNotNull(
        modifyPath: (oldPath: String) -> String
    ) {
        orNull?.let { fileLocation ->
            val path = fileLocation.asFile.path
            val newPath = modifyPath(path)
            set(File(newPath))
        }
    }
}
