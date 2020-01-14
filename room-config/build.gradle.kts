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

    testImplementation(project(":test-project"))
}

gradlePlugin {
    plugins {
        create("roomConfig") {
            id = "com.avito.android.room-config"
            implementationClass = "com.avito.android.plugin.RoomConfigPlugin"
        }
    }
}
