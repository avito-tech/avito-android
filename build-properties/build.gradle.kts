plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val androidGradlePluginVersion: String by project

dependencies {
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")

    implementation(project(":pre-build"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":kotlin-dsl-support"))

    testImplementation(project(":test-project"))
}

gradlePlugin {
    plugins {
        create("buildProperties") {
            id = "com.avito.android.build-properties"
            implementationClass = "com.avito.android.info.BuildPropertiesPlugin"
        }
    }
}
