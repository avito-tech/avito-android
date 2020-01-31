plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val funktionaleVersion: String by project
val antPatternMatcherVersion: String by project

dependencies {
    //todo жирная зависимость ради единственного Commandline.translateCommandline(source)
    implementation(gradleApi())

    implementation("org.funktionale:funktionale-try:$funktionaleVersion")

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testFixturesImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
}
