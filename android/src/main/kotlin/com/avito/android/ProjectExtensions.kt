package com.avito.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
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

fun Project.withAndroidModule(block: (baseExtension: BaseExtension) -> Unit) {
    withAndroidApp(block)
    withAndroidLib(block)
}

val Project.androidBaseExtension: BaseExtension
    get() = extensions.getByName<BaseExtension>("android")

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

fun TaskContainer.bundleTaskProvider(variant: ApplicationVariant): TaskProvider<*> =
    named("bundle${variant.name.capitalize()}")
