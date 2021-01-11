plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(Dependencies.Gradle.kotlinPlugin)

    testImplementation(project(":gradle:test-project"))
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
