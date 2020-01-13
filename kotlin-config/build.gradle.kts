plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation(project(":android"))
    implementation(project(":kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}

gradlePlugin {
    plugins {
        create("kotlinRootConfig") {
            id = "com.avito.android.kotlin-root"
            implementationClass = "com.avito.android.plugin.KotlinRootConfigPlugin"
        }
    }
}
