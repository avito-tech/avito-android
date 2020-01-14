plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project
val funktionaleVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunit5Version: String by project
val androidToolsVersion: String by project
val rxjava1Version: String by project
val truthVersion: String by project

dependencies {
    compileOnly(gradleApi())
    implementation(project(":runner:shared"))
    implementation("org.funktionale:funktionale-try:${funktionaleVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("com.android.tools.ddms:ddmlib:$androidToolsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("io.reactivex:rxjava:${rxjava1Version}")

    testImplementation(project(":test-project"))
    testImplementation(project(":runner:shared-test"))
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    testImplementation("com.google.truth:truth:$truthVersion")
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
