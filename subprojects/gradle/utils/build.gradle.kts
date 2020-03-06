plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gradle.androidPlugin)

    testImplementation(Dependencies.test.truth)
    testImplementation(Dependencies.test.mockitoKotlin2)

    testFixturesImplementation(Dependencies.kotlinStdlib)
}
