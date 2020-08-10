plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":common:report-viewer"))
    implementation(project(":common:logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:bitbucket"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:process"))
    implementation(project(":gradle:files"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:instrumentation-tests"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:git"))
    implementation(Dependencies.teamcityClient)
    implementation(Dependencies.statsd)
    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:report-viewer-test-fixtures"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.truth)
}

gradlePlugin {
    plugins {
        create("com.avito.android.performance") {
            id = "com.avito.android.performance"
            implementationClass = "com.avito.performance.PerformancePlugin"
            displayName = "Performance testing"
        }
    }
}
