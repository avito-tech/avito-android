# Release

{{<avito section>}}

We publish releases to [Bintray](https://bintray.com/avito-tech/maven/avito-android).

## Publishing a new release

1. Check current status of [Infra Gradle plugins configuration compatibility with Avito](http://links.k.avito.ru/80)
    1. If it is `Failed` you could release from previous `Succeed` commits or fix compatibility problems
1. Check current status of [Nightly Avito integration build](http://links.k.avito.ru/gZ)
    1. If it is `Failed` you could release from previous `Succeed` commits or fix problems
1. Checkout a `release branch` with a name equals to `projectVersion`. For example, `2020.9`.\
This branch must be persistent. It is used for automation.
1. Manually run [Integration build](http://links.k.avito.ru/ZA) on the `release branch`.
1. Make a PR to an internal avito repository with the new version of infrastructure
1. Checkout a new branch and make a PR to github repository:
    - Change `infraVersion` property in the `./gradle.properties` to the new version 
    - Bump up a `projectVersion` property in the `./subprojects/gradle.properties` to the next version
1. Create a new [release](https://help.github.com/en/github/administering-a-repository/managing-releases-in-a-repository) against the release branch.\
You can use a draft release to prepare a description in advance.

## Local integration tests against Avito

### Using `mavenLocal`

1. Run `./gradlew publishToMavenLocal -PprojectVersion=local` in github repository.
1. Run integration tests of your choice in avito with specified test version

### Using `compositeBuild`

Run from Avito project directory 

```shell script
./gradlew <task> -Pavito.useCompositeBuild=true -Pavito.compositeBuildPath=<avito-android-infra/subprojects dir on your local machine>
```

## CI integration tests against Avito

1. Choose configuration from [existed]({{<relref "#ci-integration-configurations">}})
1. Run build. \
If you need to test unmerged code, select a custom build branch.\
You will see branches from both repositories:

![](https://user-images.githubusercontent.com/1104540/75977180-e5dd4d80-5eec-11ea-80d3-2f9abd7efd36.png)

- By default, build uses develop from github against develop from avito
- If you pick a branch from avito, it will run against develop on github
- If you pick a branch from github, it will run against develop on avito
- To build both projects of special branch, they should have the same name

If you want to run a real CI build against not published release, 
you need to publish it manually as a temporary version to the internal Artifactory.

## CI integration configurations

- [fast check configuration (internal)](http://links.k.avito.ru/fastCheck) - pull request builds
- [integration check](http://links.k.avito.ru/ZA) - currently, contains the biggest amount of integration checks
- [nightly integration check](http://links.k.avito.ru/gZ) - the same as `integration check` but has a bigger amount of emulators
- [Gradle configuration compatibility check](http://links.k.avito.ru/80) - checks the configuration compatibility of our Gradle plugins with Avito repo  
