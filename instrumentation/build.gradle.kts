plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val gsonVersion: String by project
val kotlinCoroutinesVersion: String by project
val teamcityRestClientVersion: String by project
val retrofitVersion: String by project
val kotsonVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunit5Version: String by project
val okhttpVersion: String by project

dependencies {
    implementation(project(":instrumentation-impact-analysis"))
    implementation(project(":runner:client"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":android"))
    implementation(project(":teamcity"))
    implementation(project(":statsd"))
    implementation(project(":test-summary"))
    implementation(project(":report-viewer"))
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":git"))
    implementation(project(":process"))
    implementation(project(":files"))
    implementation(project(":slack"))
    implementation(project(":time"))
    implementation(project(":bitbucket"))
    implementation(project(":file-storage"))
    implementation(project(":sentry"))
    implementation(project(":kubernetes"))
    implementation(project(":upload-cd-build-result"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.smali:dexlib2:2.3")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")
    implementation("org.apache.commons:commons-text:1.6")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.github.salomonbrys.kotson:kotson:$kotsonVersion")

    testImplementation(project(":test-project"))
    testImplementation(testFixtures(project(":logging")))
    testImplementation(testFixtures(project(":slack")))
    testImplementation(testFixtures(project(":utils")))
    testImplementation(testFixtures(project(":report-viewer")))
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")

    testFixturesImplementation(project(":utils"))
    testFixturesImplementation(project(":test-project"))
    testFixturesImplementation(project(":bitbucket"))
    testFixturesImplementation(project(":slack"))
    testFixturesImplementation(project(":statsd"))
    testFixturesImplementation(project(":report-viewer"))
    testFixturesImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
}

gradlePlugin {
    plugins {
        create("functionalTests") {
            id = "com.avito.android.instrumentation-tests"
            implementationClass = "com.avito.instrumentation.InstrumentationTestsPlugin"
        }

        create("defaultConfig") {
            id = "com.avito.android.instrumentation-tests-default-config"
            implementationClass = "com.avito.instrumentation.InstrumentationDefaultConfigPlugin"
        }
    }
}
