plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val funktionaleVersion: String by project
val androidGradlePluginVersion: String by project

dependencies {
    implementation(project(":statsd"))
    implementation(project(":sentry"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":impact"))
    implementation(project(":trace-event"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")

    testImplementation(project(":git"))
    testImplementation(project(":test-project"))
}

gradlePlugin {
    plugins {
        create("buildMetrics") {
            id = "com.avito.android.build-metrics"
            implementationClass = "com.avito.android.plugin.build_metrics.BuildMetricsPlugin"
        }
    }
}
