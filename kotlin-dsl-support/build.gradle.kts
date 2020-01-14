plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    testImplementation(project(":test-project"))
}

gradlePlugin {
    plugins {
        create("test") {
            id = "com.avito.test"
            implementationClass = "com.avito.kotlin.dsl.TestPlugin"
        }
    }
    isAutomatedPublishing = false
}
