plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
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
