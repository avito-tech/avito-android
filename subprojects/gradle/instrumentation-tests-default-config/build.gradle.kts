plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:instrumentation-tests"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:kubernetes"))
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
