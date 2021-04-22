# Releasing infrastructure

--8<--
avito-disclaimer.md
--8<--

We publish releases to Maven Central: [com.avito.android](https://search.maven.org/search?q=com.avito.android).

## Publishing a new release

??? info "If you release for the first time"

    - [Get an access to Sonatype](#getting-access-to-sonatype) 

1. Check if [diff against the last release](https://github.com/avito-tech/avito-android/compare/2021.%3CINSERT_HERE_THE_LAST_RELEASE%3E...develop) contains any changes for users.
If not, then probably there are no reasons to make a release.
1. Check the current status of [Nightly Avito integration build](http://links.k.avito.ru/gZ).  
If it is `Failed` you could release from previous `Succeed` commits or fix problems.
1. Checkout a `release branch` with a name equals to `projectVersion`. For example, `2021.9`.  
This branch must be persistent. It is used for automation.
1. Manually run [Integration build](http://links.k.avito.ru/ZA) on the `release branch`.
1. Manually run [Github publish configuration](http://links.k.avito.ru/releaseAvitoTools) on the `release branch`. 
It will upload artifacts to a staging repository in [Sonatype](https://oss.sonatype.org/#stagingRepositories)
1. [Release staging repository](#making-a-release-in-sonatype)
1. Make a PR to an internal avito repository with the new version of infrastructure.
1. Checkout a new branch and make a PR to github repository:
    - Change `infraVersion` property in the `./gradle.properties` to the new version 
    - Bump up a `projectVersion` property in the `./gradle.properties` to the next version
1. Publish a release in Github:  
   ```sh
   make draft_release version=<current release version> prev_version=<last release version>
   ``` 
   You need to have the [`Github cli`](https://github.com/cli/cli).  
   See also more details about [Managing releases in a repository](https://help.github.com/en/github/administering-a-repository/managing-releases-in-a-repository).

### Getting access to Sonatype

1. [Create an account](https://issues.sonatype.org/secure/Signup!default.jspa)
1. Create an issue referencing [original one](https://issues.sonatype.org/browse/OSSRH-64609), asking for `com.avito.android` access
1. Wait for confirmation
1. Login to [nexus](https://oss.sonatype.org/) to validate staging profile access

Some additional info:

- [Maven central publishing reference article](https://getstream.io/blog/publishing-libraries-to-mavencentral-2021/)

### Making a release in Sonatype

We publish a release through a temporary staging repository. 
If something goes wrong you can drop the repository to cancel the publication process.

1. Open [Staging repositories](https://oss.sonatype.org/#stagingRepositories)
![oss-avito](https://user-images.githubusercontent.com/1104540/109542777-92d5b080-7ad6-11eb-9393-30adfa11f749.png)
In a Content tab you can see uploaded artifacts.
1. Close the repository:  
You don’t need to provide a description here.
![oss-close](https://user-images.githubusercontent.com/1104540/109543602-8ef65e00-7ad7-11eb-850d-70542451ee94.png)
In an Activity tab you can track progress.
![oss-release](https://user-images.githubusercontent.com/1104540/109543639-9ae22000-7ad7-11eb-82d4-d3d2c1975521.png)
1. Release the repository. It will publish the contents to Maven Central
![oss-release-confirm](https://user-images.githubusercontent.com/1104540/109543687-ac2b2c80-7ad7-11eb-8294-7d603c523156.png)
1. Wait till new packages appear on [Maven Central](https://search.maven.org/search?q=com.avito.android). Should take ~15 min.

Some additional info:

- [Maven central publishing reference article](https://getstream.io/blog/publishing-libraries-to-mavencentral-2021/)

### Known issues

#### Can't find an artifact in an internal Artifactory

How it looks:

- Maven central has [expected artifacts](https://search.maven.org/search?q=com.avito.android): pom, jar/aar, sources.jar
- Gradle can't find it in Artifactory Proxy

```text
> Could not resolve all artifacts for configuration ':classpath'.
   > Could not find com.avito.android:runner-shared:2020.16.
     Searched in the following locations:
       - file:/home/user/.m2/repository/
       - http://<artifactory>/
```

Probable reasons:

- The file is not downloaded by Artifactory yet. 
Such files look in web UI like empty references: `runner-shared-2020.16.jar->     -    -    -    -` (empty size)
- When you use a partially uploaded release, Artifactory might cache the wrong state.
It seems that Artifactory caches it for some time, but we don't know exactly and how to invalidate it.

Actions:

- Download this file manually in the browser or CLI.  
If the file downloaded successfully, refresh a local cache via `--refresh-dependencies`.
- If it didn't help, bump up a minor release version and make a new release. 

## Local integration tests against Avito

### Using `mavenLocal`

1. Run `make publish_to_maven_local` in github repository.
1. Run integration tests of your choice in avito with specified test version

### Using `compositeBuild`

Run from Avito project directory 

```shell
./gradlew <task> -Pavito.useCompositeBuild=true -Pavito.compositeBuildPath=<avito-android-infra/subprojects dir on your local machine>
```

## CI integration tests against Avito

1. Choose configuration from [existed](#ci-integration-configurations)
1. Run build.  
   If you need to test unmerged code, select a custom build branch.  
   You will see branches from both repositories:

![](https://user-images.githubusercontent.com/1104540/75977180-e5dd4d80-5eec-11ea-80d3-2f9abd7efd36.png)

- By default, build uses develop from github against develop from avito
- If you pick a branch from avito, it will run against develop on github
- If you pick a branch from github, it will run against develop on avito
- To build both projects of special branch, they should have the same name

## CI integration configurations

- [fast check configuration (internal)](http://links.k.avito.ru/fastCheck) - pull request's builds
- [integration check](http://links.k.avito.ru/ZA) - currently, contains the biggest amount of integration checks
- [nightly integration check](http://links.k.avito.ru/gZ) - the same as `integration check` but uses more Android emulators
- [Gradle configuration compatibility check](http://links.k.avito.ru/80) - checks the configuration compatibility of our Gradle plugins with Avito repo  
