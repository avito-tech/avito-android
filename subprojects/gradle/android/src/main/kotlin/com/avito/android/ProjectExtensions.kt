@file:Suppress("DEPRECATION")

package com.avito.android

import com.android.build.api.component.ComponentIdentity
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestedExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType

public fun Project.withAndroidApp(block: (appExtension: AppExtension) -> Unit) {
    plugins.withType<AppPlugin> { block(androidAppExtension) }
}

public fun Project.withAndroidLib(block: (appExtension: LibraryExtension) -> Unit) {
    plugins.withType<LibraryPlugin> { block(androidLibraryExtension) }
}

/**
 * TODO somehow withType on deprecated BasePlugin works,
 *  and construction: `plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded { }` not
 */
public fun Project.withAndroidModule(block: (testedExtension: TestedExtension) -> Unit) {
    plugins.withType<BasePlugin> { block(testedExtension) }
}

@Suppress("UnstableApiUsage")
public val Project.androidCommonExtension: CommonExtension<*, *, *, *, *, *, *, *>
    get() = extensions.getByType(CommonExtension::class.java)

public val Project.androidBaseExtension: BaseExtension
    get() = extensions.getByName<BaseExtension>("android")

public val Project.testedExtension: TestedExtension
    get() = extensions.getByName<TestedExtension>("android")

public val Project.androidAppExtension: AppExtension
    get() = androidBaseExtension as AppExtension

public val Project.androidLibraryExtension: LibraryExtension
    get() = androidBaseExtension as LibraryExtension

public fun Project.isAndroid(): Boolean =
    isAndroidLibrary() || isAndroidApp()

public fun Project.isAndroidApp(): Boolean =
    plugins.hasPlugin("com.android.application")

public fun Project.isAndroidLibrary(): Boolean =
    plugins.hasPlugin("com.android.library")

@Suppress("DefaultLocale")
public fun TaskContainer.packageTaskProvider(variantName: String): TaskProvider<*> =
    named("package${variantName.capitalize()}")

@Suppress("DefaultLocale")
public fun TaskContainer.bundleTaskProvider(variantName: String): TaskProvider<*> =
    named("bundle${variantName.capitalize()}")

@Suppress("DefaultLocale", "UnstableApiUsage")
public fun taskName(prefix: String, component: ComponentIdentity): String =
    prefix + component.flavorName.capitalize() + component.buildType.orEmpty().capitalize()
