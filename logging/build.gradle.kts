plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project
val jslackVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.github.seratch:jslack:$jslackVersion")
    implementation(project(":utils"))
    implementation(project(":sentry"))
    implementation(project(":kotlin-dsl-support"))

    testFixturesImplementation(project(":utils"))
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
