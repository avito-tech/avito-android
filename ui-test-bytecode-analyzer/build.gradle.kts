plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val bcelVersion: String by project
val gsonVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.apache.bcel:bcel:$bcelVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")

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
