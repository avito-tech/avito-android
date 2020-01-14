plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project
val funktionaleVersion: String by project
val gsonVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunit5Version: String by project

dependencies {
    compileOnly(gradleApi())
    compile(project(":runner:shared"))
    compile(project(":runner:service"))

    implementation(project(":trace-event"))
    implementation("org.funktionale:funktionale-try:${funktionaleVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")

    testImplementation(project(":test-project"))
    testImplementation(project(":runner:shared-test"))
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")
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
