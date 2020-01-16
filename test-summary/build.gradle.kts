plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":report-viewer"))
    implementation(project(":statsd"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":slack"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    testImplementation(project(":test-project"))
    testImplementation(testFixtures(project(":report-viewer")))

    testFixturesImplementation(project(":report-viewer"))
}
