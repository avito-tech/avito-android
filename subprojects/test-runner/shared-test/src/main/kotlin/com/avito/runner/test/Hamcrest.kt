package com.avito.runner.test

@Suppress("HasPlatformType") // unspecified return type is required to allow compiler to infer type in caller code
fun <T> Is(value: T) = org.hamcrest.core.Is.`is`(value)
