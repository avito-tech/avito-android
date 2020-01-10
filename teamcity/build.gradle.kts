plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val teamcityRestClientVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.teamcity:teamcity-rest-client:$teamcityRestClientVersion")

    testImplementation(project(":test-project"))
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
