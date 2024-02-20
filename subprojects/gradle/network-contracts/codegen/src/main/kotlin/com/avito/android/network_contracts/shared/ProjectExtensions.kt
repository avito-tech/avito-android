package com.avito.android.network_contracts.shared

import com.avito.android.network_contracts.extension.NetworkContractsModuleExtension
import com.avito.android.network_contracts.extension.NetworkContractsRootExtension
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import java.io.File

internal fun Project.findPackageDirectory(packageName: Provider<String>): Provider<Directory> {
    val packageFilePath = packageName.map { it.replace(".", File.separator) }
    return mainSourceSetDirectory().dir(packageFilePath)
}

internal fun Project.mainSourceSetDirectory(): Directory {
    val mainDirectory = project.layout.projectDirectory.dir("src")
        .dir("main")

    return mainDirectory.dir("java").takeIf { it.asFile.exists() }
        ?: mainDirectory.dir("kotlin")
}

internal val Project.networkContractsExtension: NetworkContractsModuleExtension
    get() = extensions.getByType<NetworkContractsModuleExtension>()

internal val Project.networkContractsRootExtension: NetworkContractsRootExtension
    get() = rootProject.extensions.getByType<NetworkContractsRootExtension>()

internal fun Project.reportFile(directory: String, reportFileName: String): Provider<RegularFile> {
    return project.layout.buildDirectory
        .dir("reports")
        .map { it.dir(directory) }
        .map { it.file(reportFileName) }
}
