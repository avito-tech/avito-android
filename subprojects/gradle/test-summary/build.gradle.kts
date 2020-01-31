plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinCoroutinesVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:statsd"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:slack"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))

    testFixturesImplementation(project(":subprojects:common:report-viewer"))
}
