plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
}

dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("cdContract") {
            id = "com.avito.android.upload-cd-build-result"
            implementationClass = "com.avito.cd.UploadCdBuildResultPlugin"
            displayName = "CD Contract Plugin"
        }
    }
}
