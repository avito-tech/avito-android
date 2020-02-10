plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
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
