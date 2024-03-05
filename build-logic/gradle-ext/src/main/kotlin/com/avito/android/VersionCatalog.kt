package com.avito.android

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project

val Project.libs
    get() = extensions.getByType(LibrariesForLibs::class.java)
