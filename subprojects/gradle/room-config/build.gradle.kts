plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(libs.kotlinPlugin)
    implementation(libs.kotlinStdlib)

    testImplementation(project(":gradle:test-project"))

    gradleTestImplementation(project(":gradle:test-project"))
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
