plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:android"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(Dependencies.Gradle.kotlinPlugin)
}

gradlePlugin {
    plugins {
        create("kotlinRootConfig") {
            id = "com.avito.android.kotlin-root"
            implementationClass = "com.avito.android.plugin.KotlinRootConfigPlugin"
            displayName = "Kotlin config"
        }
    }
}
