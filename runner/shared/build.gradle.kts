plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val rxjava1Version: String by project

dependencies {
    compileOnly(gradleApi())
    implementation("io.reactivex:rxjava:${rxjava1Version}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
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
