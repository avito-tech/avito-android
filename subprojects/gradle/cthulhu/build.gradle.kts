plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:ci-logger"))

    implementation(project(":common:report-viewer"))
    implementation(project(":common:time"))

    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)
}

gradlePlugin {
    plugins {
        create("cthulhu") {
            id = "com.avito.android.cthulhu"
            implementationClass = "com.avito.plugin.CthulhuPlugin"
            displayName = "Cthulhu (Avito TMS) integration plugin"
        }
    }
}
