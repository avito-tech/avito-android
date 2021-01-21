@file:Suppress("DEPRECATION")

package com.avito.android

import com.android.build.api.component.ComponentIdentity
import com.android.build.api.extension.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType

fun Project.withAndroidApp(block: (appExtension: AppExtension) -> Unit) {
    plugins.withType<AppPlugin> { block(androidAppExtension) }
}

fun Project.withAndroidLib(block: (appExtension: LibraryExtension) -> Unit) {
    plugins.withType<LibraryPlugin> { block(androidLibraryExtension) }
}

/**
 * TODO somehow withType on deprecated BasePlugin works,
 *  and construction: `plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded { }` not
 */
fun Project.withAndroidModule(block: (testedExtension: TestedExtension) -> Unit) {
    plugins.withType<BasePlugin> { block(testedExtension) }
}

@Suppress("UnstableApiUsage")
fun Project.androidComponents(block: AndroidComponentsExtension<*, *>.() -> Unit) {
    block(extensions.getByType(AndroidComponentsExtension::class.java))
}

val Project.androidBaseExtension: BaseExtension
    get() = extensions.getByName<BaseExtension>("android")

val Project.testedExtension: TestedExtension
    get() = extensions.getByName<TestedExtension>("android")

val Project.androidAppExtension: AppExtension
    get() = androidBaseExtension as AppExtension

val Project.androidLibraryExtension: LibraryExtension
    get() = androidBaseExtension as LibraryExtension

fun Project.isAndroid(): Boolean =
    isAndroidLibrary() || isAndroidApp()

fun Project.isAndroidApp(): Boolean =
    plugins.hasPlugin("com.android.application")

fun Project.isAndroidLibrary(): Boolean =
    plugins.hasPlugin("com.android.library")

@Suppress("DefaultLocale")
fun TaskContainer.bundleTaskProvider(variant: ApplicationVariant): TaskProvider<*> =
    named("bundle${variant.name.capitalize()}")

@Suppress("DefaultLocale", "UnstableApiUsage")
fun taskName(prefix: String, component: ComponentIdentity) =
    prefix + component.flavorName.capitalize() + component.buildType.orEmpty().capitalize()
