# Infrastructure release

--8<--
avito-disclaimer.md
--8<--

We publish releases to [Bintray](https://bintray.com/avito-tech/maven/avito-android).

## Publishing a new release

1. Check if [diff against the last release](https://github.com/avito-tech/avito-android/compare/2020.%3CSELECT_HERE_THE_LAST_RELEASE%3E...develop) contains any changes for users.
If not, then probably there are no reasons to make a release.
1. Check current status of [Infra Gradle plugins configuration compatibility with Avito](http://links.k.avito.ru/80).\
If it is `Failed` you could release from previous `Succeed` commits or fix compatibility problems.
1. Check current status of [Nightly Avito integration build](http://links.k.avito.ru/gZ).\
If it is `Failed` you could release from previous `Succeed` commits or fix problems.
1. Checkout a `release branch` with a name equals to `projectVersion`. For example, `2020.9`.\
This branch must be persistent. It is used for automation.
1. Manually run [Integration build](http://links.k.avito.ru/ZA) on the `release branch`.
1. Manually run [Github publish configuration](http://links.k.avito.ru/releaseAvitoTools) on the `release branch`.
1. Make a PR to an internal avito repository with the new version of infrastructure.
1. Checkout a new branch and make a PR to github repository:
    - Change `infraVersion` property in the `./gradle.properties` to the new version 
    - Bump up a `projectVersion` property in the `./subprojects/gradle.properties` to the next version
1. Run `make draft_release version=<current release version> prev_version=<last release version>`
([Managing releases in a repository](https://help.github.com/en/github/administering-a-repository/managing-releases-in-a-repository))\

### Known issues

#### Failed publishing to Bintray

Uploading to Bintray is flaky. You can face different issues:

- NoHttpResponseException: api.bintray.com:443 failed to respond [#325](https://github.com/bintray/gradle-bintray-plugin/issues/325)
- Could not upload to https://api.bintray.com/...: HTTP/1.1 405 Not Allowed nginx

In this case artifacts can be uploaded partially, only pom for instance.\
Try to upload it with overriding:

0. Enable `BintrayExtension.override` in a buildscript.
0. Upload problematic artifact:\
`./gradlew -p subprojects :<module>:bintrayUpload --no-parallel --stacktrace`

#### Can't find an artifact in an internal Artifactory

How it looks:

- Bintray has [expected artifacts](https://dl.bintray.com/avito/maven/com/avito/android/runner-shared/2020.16/): pom, jar/aar, sources.jar
- Gradle can't find it in Artifactory

```text
> Could not resolve all artifacts for configuration ':classpath'.
   > Could not find com.avito.android:runner-shared:2020.16.
     Searched in the following locations:
       - file:/home/user/.m2/repository/com/avito/android/runner-shared/2020.16/runner-shared-2020.16.pom
       - http://<artifactory>/artifactory/bintray-avito-maven/com/avito/android/runner-shared/2020.16/runner-shared-2020.16.pom
```

Probable reasons:

- The file is not downloaded by Artifactory yet. 
Such files look in web UI like empty references: `runner-shared-2020.16.jar->     -    -    -    -` (empty size)
- When you use a partially uploaded release, Artifactory might cache the wrong state.
It seems that Artifactory caches it for some time, but we don't know exactly and how to invalidate it.

Actions:

- Download this file manually in the browser or CLI.\
If the file downloaded successfully, refresh a local cache via `--refresh-dependencies`.
- If it didn't help, bump up a minor release version and make a new release. 

## Local integration tests against Avito

### Using `mavenLocal`

1. Run `./gradlew publishToMavenLocal -PprojectVersion=local -p subprojects` in github repository.
1. Run integration tests of your choice in avito with specified test version

### Using `compositeBuild`

Run from Avito project directory 

```shell
./gradlew <task> -Pavito.useCompositeBuild=true -Pavito.compositeBuildPath=<avito-android-infra/subprojects dir on your local machine>
```

## CI integration tests against Avito

1. Choose configuration from [existed](#ci-integration-configurations)
1. Run build. \
   If you need to test unmerged code, select a custom build branch.\
   You will see branches from both repositories:

![](https://user-images.githubusercontent.com/1104540/75977180-e5dd4d80-5eec-11ea-80d3-2f9abd7efd36.png)

- By default, build uses develop from github against develop from avito
- If you pick a branch from avito, it will run against develop on github
- If you pick a branch from github, it will run against develop on avito
- To build both projects of special branch, they should have the same name

If you want to run a real CI build against not published release, 
you could publish it manually as a temporary version to the internal Artifactory:

```text
./gradlew -p subprojects publishToArtifactory -PprojectVersion=<projectVersion>-integration-01  -Dorg.gradle.internal.publish.checksums.insecure=true
```

Or using [Publish to Artifactory](http://links.k.avito.ru/publishToArtifactoryConfiguration) CI configuration.

## CI integration configurations

- [fast check configuration (internal)](http://links.k.avito.ru/fastCheck) - pull request's builds
- [integration check](http://links.k.avito.ru/ZA) - currently, contains the biggest amount of integration checks
- [nightly integration check](http://links.k.avito.ru/gZ) - the same as `integration check` but uses more Android emulators
- [Gradle configuration compatibility check](http://links.k.avito.ru/80) - checks the configuration compatibility of our Gradle plugins with Avito repo  
