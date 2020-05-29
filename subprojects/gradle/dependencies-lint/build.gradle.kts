plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:cd"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:files"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:kotlin-dsl-support"))

    implementation(Dependencies.gradle.androidPlugin)
    implementation(Dependencies.gradle.kotlinPlugin)
    implementation(Dependencies.asm)

    testImplementation(project(":gradle:test-project"))
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
