plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.gradle-testing")
}

tasks.processResources {
    val r8Version = libs.versions.r8.get()
    inputs.property("r8Version", r8Version)
    filesMatching("com/avito/test/gradle/r8-version.txt") {
        expand("r8Version" to r8Version)
    }
}

dependencies {
    api(gradleTestKit())

    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:truth-extensions"))
    implementation(project(":subprojects:logger:logger"))

    implementation(libs.androidToolsCommon)
    implementation(libs.kotlinReflect)
    implementation(libs.truth)
}
