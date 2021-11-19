# How to start

## Project structure

- `subprojects` contains almost all of source code divided by project modules

- `samples` separate gradle project that includeBuild("subprojects") to iterate on changes fast and provide samples

- `ci/` and `/*.sh` - we follow [IaC](https://en.wikipedia.org/wiki/Infrastructure_as_code) principle whenever possible.
  You can see docker images we use to abstract configuration of apps building and testing, as well as testing github
  project itself.
- `docs/` - documentation you see right now and code to deploy it automatically.  
  [How we document](Documentation.md)
  
## Makefile

Take a look at `./Makefile` for useful commands/shortcuts

## Build features to consider enabling

- env `COMMIT_CHECK=true` to run optimal amount of CI checks on git pre-commit hook
- env `CONFIG_CACHE=true` to enable [Gradle configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html) for Makefile commands.  
You can also override it per command: `make command CONFIG_CACHE=false`.

### Avito employees only

- [Gradle remote cache](internal/RemoteCache.md) for local builds <Avito only>
- `artifactoryUrl` property in `<GRADLE_USER_HOME>/gradle.properties` to download dependencies from in-house proxy
