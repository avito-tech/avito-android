plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
}
dependencies {
    implementation(project(":subprojects:gradle:module-types-api"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:clickstream"))
}

gradlePlugin {
    plugins {
        create("demo-app-metrics") {
            id = "com.avito.android.demo-app-metrics"
            implementationClass = "com.avito.android.DemoAppMetricsPlugin"
        }
    }
}
