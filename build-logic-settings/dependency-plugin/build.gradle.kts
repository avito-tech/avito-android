plugins {
    java
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":extensions"))
}

gradlePlugin {
    plugins {
        create("dependency-management") {
            id = "convention-dependencies"
            implementationClass = "com.avito.DependencyResolutionPlugin"
        }
    }
}
