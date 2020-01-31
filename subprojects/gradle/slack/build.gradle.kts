plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val jslackVersion: String by project
val funktionaleVersion: String by project
val kotlinCoroutinesVersion: String by project

dependencies {
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:common:time"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.github.seratch:jslack:$jslackVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:time")))

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testFixturesImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
}
