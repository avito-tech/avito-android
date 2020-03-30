plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}

repositories {
    jcenter()
}

gradlePlugin {
    plugins {
        create("testKitHelper") {
            id = "com.avito.test.gradle.helper"
            implementationClass = "TestKitHelperPlugin"
            displayName = "TestKit Helper Plugin"
        }
    }
}
