package com.avito.performance

import com.avito.utils.runCommand
import org.gradle.api.Project

internal interface CommitAncestorDetector {

    fun isParent(ancestor: String, child: String): Boolean

    class Impl(private val project: Project) : CommitAncestorDetector {

        override fun isParent(ancestor: String, child: String): Boolean {
            return runCommand(
                command = "git merge-base --is-ancestor $ancestor $child",
                workingDirectory = project.rootProject.rootDir
            ).isSuccess()
        }
    }
}
