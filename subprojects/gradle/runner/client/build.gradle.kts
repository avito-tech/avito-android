plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-client"

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project
val funktionaleVersion: String by project
val gsonVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunit5Version: String by project

dependencies {
    compileOnly(gradleApi())
    compile(project(":subprojects:gradle:runner:shared"))
    compile(project(":subprojects:gradle:runner:service"))

    implementation(project(":subprojects:gradle:trace-event"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:runner:shared-test"))
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")
}
