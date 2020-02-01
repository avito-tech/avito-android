package com.avito.android.sentry

import org.gradle.api.Project

/**
 * Use [Project.environmentInfo] to gain instance
 */
interface EnvironmentInfo {
    val node: String?
    val environment: Environment
    val commit: String?
    fun teamcityBuildId(): String?
    fun isInvokedFromIde(): Boolean
}

/**
 * @param publicName это название используется в graphite событиях
 */
sealed class Environment(val publicName: String) {
    object Local : Environment("local")
    object Mirakle : Environment("mirakle")
    object CI : Environment("ci")
    object Unknown : Environment("_")
}
