plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(Dependencies.gradle.kotlinPlugin)
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
