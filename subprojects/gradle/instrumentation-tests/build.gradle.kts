plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

val gsonVersion: String by project
val kotlinCoroutinesVersion: String by project
val teamcityRestClientVersion: String by project
val retrofitVersion: String by project
val funktionaleVersion: String by project
val kotsonVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunit5Version: String by project
val okhttpVersion: String by project

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation(project(":subprojects:gradle:instrumentation-test-impact-analysis"))
    implementation(project(":subprojects:gradle:runner:client"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:test-summary"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:slack"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:gradle:kubernetes"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))
    implementation("org.smali:dexlib2:2.3")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")
    implementation("org.apache.commons:commons-text:1.6")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:gradle:logging")))
    testImplementation(testFixtures(project(":subprojects:gradle:slack")))
    testImplementation(testFixtures(project(":subprojects:gradle:utils")))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")

    testFixturesImplementation(project(":subprojects:gradle:kubernetes"))
    testFixturesImplementation(project(":subprojects:gradle:utils"))
    testFixturesImplementation(project(":subprojects:gradle:test-project"))
    testFixturesImplementation(project(":subprojects:gradle:bitbucket"))
    testFixturesImplementation(project(":subprojects:gradle:slack"))
    testFixturesImplementation(project(":subprojects:common:statsd"))
    testFixturesImplementation(project(":subprojects:common:report-viewer"))
    testFixturesImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testFixturesImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
}

gradlePlugin {
    plugins {
        create("functionalTests") {
            id = "com.avito.android.instrumentation-tests"
            implementationClass = "com.avito.instrumentation.InstrumentationTestsPlugin"
            displayName = "Instrumentation tests"
        }

        create("defaultConfig") {
            id = "com.avito.android.instrumentation-tests-default-config"
            implementationClass = "com.avito.instrumentation.InstrumentationDefaultConfigPlugin"
            displayName = "Instrumentation tests default configuration"
        }
    }
}
