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
    implementation(project(":utils:"))
    implementation(project(":logging"))
    implementation(project(":time"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.github.seratch:jslack:$jslackVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    testImplementation(project(":test-project"))
    testImplementation(testFixtures(project(":time")))

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    testFixturesImplementation("org.funktionale:funktionale-try:$funktionaleVersion")
}

//todo withSourcesJar 6.0 gradle
val sourcesTask = tasks.create<Jar>("sourceJar") {
    classifier = "sources"
    from(sourceSets.main.get().allJava)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourcesTask)
        }
    }
}
