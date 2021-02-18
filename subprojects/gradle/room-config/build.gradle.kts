plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin-legacy")
    id("convention.libraries")
}

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(libs.kotlinPlugin)

    testImplementation(project(":subprojects:gradle:test-project"))
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
