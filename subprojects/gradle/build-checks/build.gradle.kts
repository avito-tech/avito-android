plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val funktionaleVersion: String by project
val androidGradlePluginVersion: String by project
val statsdVersion: String by project
val sentryVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:impact"))
    implementation(project(":subprojects:gradle:build-metrics"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("com.timgroup:java-statsd-client:$statsdVersion")
    implementation("io.sentry:sentry:$sentryVersion")

    //for test
    implementation(project(":subprojects:gradle:module-type"))

    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("buildChecks") {
            id = "com.avito.android.buildchecks"
            implementationClass = "com.avito.android.plugin.build_param_check.BuildParamCheckPlugin"
        }
    }
}
