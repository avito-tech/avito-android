plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

publish {
    artifactId.set("command-line-executor")
}

dependencies {
    implementation(libs.rxJava)
}
