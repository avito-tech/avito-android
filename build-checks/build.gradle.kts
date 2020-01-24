plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val androidGradlePluginVersion: String by project
val statsdVersion: String by project
val sentryVersion: String by project

dependencies {
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":android"))
    implementation(project(":impact"))
    implementation(project(":build-metrics"))
    implementation(project(":sentry"))
    implementation(project(":statsd"))
    implementation(project(":kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("com.timgroup:java-statsd-client:$statsdVersion")
    implementation("io.sentry:sentry:$sentryVersion")

    //for test
    implementation(project(":module-type"))

    testImplementation(project(":test-project"))
}

gradlePlugin {
    plugins {
        create("buildChecks") {
            id = "com.avito.android.buildchecks"
            implementationClass = "com.avito.android.plugin.build_param_check.BuildParamCheckPlugin"
        }
    }
}
