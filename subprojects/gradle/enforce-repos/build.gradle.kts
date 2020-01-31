plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

gradlePlugin {
    plugins {
        create("enforceRepos") {
            id = "com.avito.android.enforce-repos"
            implementationClass = "EnforceReposPlugin"
        }
    }
}
