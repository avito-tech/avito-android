plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

gradlePlugin {
    plugins {
        create("enforceRepos") {
            id = "com.avito.android.enforce-repos"
            implementationClass = "com.avito.android.plugin.EnforceReposPlugin"
        }
    }
}
