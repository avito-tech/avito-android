plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val funktionaleVersion: String by project
val mockitoJunit5Version: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:utils")) // project.buildEnvironment only
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")

    testFixturesImplementation(project(":subprojects:gradle:test-project"))
    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
