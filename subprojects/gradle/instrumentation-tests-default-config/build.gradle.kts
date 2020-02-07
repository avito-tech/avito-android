plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:instrumentation-tests"))
}

gradlePlugin {
    plugins {
        create("defaultConfig") {
            id = "com.avito.android.instrumentation-tests-default-config"
            implementationClass = "com.avito.instrumentation.InstrumentationDefaultConfigPlugin"
            displayName = "Instrumentation tests default configuration"
        }
    }
}
