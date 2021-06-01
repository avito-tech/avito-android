package com.avito.utils.gradle

sealed class Environment {
    object Local : Environment()
    object Mirakle : Environment()
    object CI : Environment()
    object Unknown : Environment()
}
