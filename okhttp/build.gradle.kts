plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val okhttpVersion: String by project
val retrofitVersion: String by project

dependencies {
    implementation(project(":utils"))
    implementation(project(":kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation(project(":test-okhttp"))
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    testImplementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
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
