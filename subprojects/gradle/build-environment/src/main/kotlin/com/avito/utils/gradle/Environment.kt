package com.avito.utils.gradle

/**
 * @param publicName is used in graphite events
 */
sealed class Environment(val publicName: String) {
    object Local : Environment("local")
    object Mirakle : Environment("mirakle")
    object CI : Environment("ci")
    object Unknown : Environment("_")
}
