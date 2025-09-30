plugins {
    id("convention.kotlin-jvm")
    id("convention.kotlin-serialization")
    id("convention.unit-testing")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

application {
    mainClass.set("com.avito.android.instant_feedback.ApplicationKt")
}

dependencies {
    implementation(project(":subprojects:logger:logger"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:common:problem"))

    implementation(libs.ktorCore)
    implementation(libs.ktorNetty)
    implementation(libs.ktorContentNegotiation)
    implementation(libs.ktorKotlinSerialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.okhttp)

    testImplementation(libs.kotlinTestJUnit)
    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.ktorClientContentNegotiation)
    testImplementation(libs.coroutinesTest)
}

tasks.shadowJar {
    archiveBaseName.set("instant-feedback")
}
