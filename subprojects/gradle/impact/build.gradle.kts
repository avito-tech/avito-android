plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val funktionaleVersion: String by project
val antPatternMatcherVersion: String by project
val mockitoKotlinVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation("io.github.azagniotov:ant-style-path-matcher:$antPatternMatcherVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:gradle:logging")))
    testImplementation(testFixtures(project(":subprojects:gradle:git")))
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
}
