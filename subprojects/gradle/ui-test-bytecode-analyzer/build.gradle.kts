plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("com.avito.test.gradle.helper")
}

dependencies {
    implementation(Dependencies.bcel)
    implementation(Dependencies.gson)

    testKit(Dependencies.gradle.kotlinPlugin)
    testKit(Dependencies.gradle.androidPlugin)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(Dependencies.kotlinPoet)
}
