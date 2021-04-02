plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.android)
    implementation(projects.gradle.gradleExtensions)
    implementation(libs.kotlinPlugin)

    testImplementation(projects.gradle.testProject)

    gradleTestImplementation(projects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("roomConfig") {
            id = "com.avito.android.room-config"
            implementationClass = "com.avito.android.plugin.RoomConfigPlugin"
            displayName = "Room config"
        }
    }
}
