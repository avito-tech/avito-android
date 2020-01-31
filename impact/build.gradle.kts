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
    implementation(project(":utils"))
    implementation(project(":process"))
    implementation(project(":logging"))
    implementation(project(":android"))
    implementation(project(":git"))
    implementation(project(":kotlin-dsl-support"))
    implementation("io.github.azagniotov:ant-style-path-matcher:$antPatternMatcherVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testImplementation(project(":test-project"))
    testImplementation(testFixtures(project(":logging")))
    testImplementation(testFixtures(project(":git")))
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
}
