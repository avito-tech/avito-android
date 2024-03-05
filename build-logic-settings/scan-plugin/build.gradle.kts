plugins {
    java
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":extensions"))
    implementation("com.gradle.enterprise:com.gradle.enterprise.gradle.plugin:3.12")
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
