plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val sentryVersion: String by project

dependencies {
    api("io.sentry:sentry:$sentryVersion")

    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation(project(":utils"))
    implementation(project(":git"))
    implementation(project(":kotlin-dsl-support"))
    // can't use logging due to cyclic dependency
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
