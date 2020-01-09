plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val statsdVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation("com.timgroup:java-statsd-client:$statsdVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":kotlin-dsl-support"))
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
