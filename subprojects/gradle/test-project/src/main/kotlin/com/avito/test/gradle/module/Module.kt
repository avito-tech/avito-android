package com.avito.test.gradle.module

import com.avito.test.gradle.Generator

interface Module : Generator {
    val name: String
    val plugins: List<String>
    val buildGradleExtra: String
    val modules: List<Module>
}
