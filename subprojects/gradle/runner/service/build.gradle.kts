plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-service"

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project
val funktionaleVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunit5Version: String by project
val androidToolsVersion: String by project
val rxjava1Version: String by project
val truthVersion: String by project

dependencies {
    compileOnly(gradleApi())
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.android.tools.ddms:ddmlib:$androidToolsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("io.reactivex:rxjava:$rxjava1Version")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:runner:shared-test"))
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("com.google.truth:truth:$truthVersion")
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")
}
