package com.avito.android.module_type.validation.configurations.missings.implementations.internal

internal class ProjectPathBuilder(
    basePath: String
) {

    var path: String = basePath
        private set

    fun calculateNextDependency(dependencyPath: String, dependencyLevel: Int) {
        val previousPaths = this.path.split(PATH_SEPARATOR)
        val previousLevel = previousPaths.size

        if (previousLevel > dependencyLevel) {
            clearLastLevels(previousLevel - dependencyLevel)
        }

        this.path = "${this.path} -> $dependencyPath"
    }

    private fun clearLastLevels(count: Int) {
        val previousPaths = this.path.split(PATH_SEPARATOR)
        path = previousPaths.take(previousPaths.size - count)
            .joinToString(
                separator = " $PATH_SEPARATOR ",
                transform = String::trim
            )
    }

    companion object {

        private const val PATH_SEPARATOR = "->"
    }
}
