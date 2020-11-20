package com.avito.android.test

// see TestMinimized.md

/**
 * An analog of [androidx.annotation.Keep]
 * The difference is in intention.
 * It is used only with minimized test APK.
 * See proguad config.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class KeepForTesting

/**
 * An analog of [androidx.annotation.Keep]
 * The difference is in intention.
 * It is used only with minimized test APK.
 * See proguad config.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class KeepSyntheticConstructorsForTesting
