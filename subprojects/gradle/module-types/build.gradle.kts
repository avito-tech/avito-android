plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:pre-build"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:impact"))
    implementation(Dependencies.Gradle.kotlinPlugin)

    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("moduleTypes") {
            id = "com.avito.android.module-types"
            implementationClass = "com.avito.android.ModuleTypesPlugin"
            displayName = "Module types"
        }
    }
}
