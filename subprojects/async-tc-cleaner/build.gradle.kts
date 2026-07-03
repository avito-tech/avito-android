plugins {
    id("convention.kotlin-jvm")
    id("convention.unit-testing")
    alias(libs.plugins.shadow)
    application
}

application {
    mainClass.set("com.avito.android.async_tc_cleaner.ApplicationKt")
}

dependencies {
    // TODO: MBSA-1927 revert to libs.coroutinesCore after upgrading coroutines lib
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation(project(":subprojects:logger:logger"))
    implementation(project(":subprojects:logger:elastic-logger"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:common:statsd"))
    implementation(project(":subprojects:common:series"))
    implementation(project(":subprojects:common:elastic"))

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

tasks.shadowJar {
    archiveBaseName.set("async-tc-cleaner")
}
