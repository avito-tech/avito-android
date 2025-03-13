plugins {
    java
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":extensions"))
    implementation("com.gradle.develocity:com.gradle.develocity.gradle.plugin:3.19")
}

gradlePlugin {
    plugins {
        create("scanSettings") {
            id = "scan-settings"
            implementationClass = "com.avito.ScanSettingsPlugin"
            displayName = "Build trace"
        }
    }
}
