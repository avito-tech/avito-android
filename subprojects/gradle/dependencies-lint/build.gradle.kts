plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:cd"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    implementation(Dependencies.gradle.androidPlugin)
    implementation(Dependencies.gradle.kotlinPlugin)
    implementation(Dependencies.asm)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(Dependencies.test.mockitoKotlin)
}

gradlePlugin {
    plugins {
        create("dependenciesLintPlugin") {
            id = "com.avito.android.dependencies-lint"
            implementationClass = "com.avito.android.lint.DependenciesLintPlugin"
            displayName = "Dependencies lint"
        }
    }
}
