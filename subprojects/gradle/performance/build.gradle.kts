plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:instrumentation-tests"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:git"))
    implementation(Dependencies.teamcityClient)
    implementation(Dependencies.statsd)
    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttpLogging)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
    testImplementation(Dependencies.test.mockitoKotlin2)
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
