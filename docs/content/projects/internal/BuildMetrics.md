# Build metrics

???+ warning
    This plugin is internal and not recommended to use.  
    See alternatives: 
    [Gradle Enterprise](https://gradle.com/gradle-enterprise-solution-overview/), 
    [Talaiot](https://github.com/cdsap/Talaiot)

## Configuring

### Disabling plugin

`avito.build.metrics.enabled=false`

## Metrics

All metrics have common prefix: `<namespace>.<environment>.<node>.id.<build status>`

- Namespace: statsd prefix from `avito.stats.namespace` property
- Environment: `ci` | `local` | `mirakle` | `_` (unknown)
- Node: git username for local builds, hostname for CI builds
- Build status: `success` | `failure`

See [Environment](https://github.com/avito-tech/avito-android/blob/develop/subprojects/gradle/sentry-config/src/main/kotlin/com/avito/android/sentry/EnvironmentInfo.kt) 

### Common build metrics

- `init_configuration.total` (time in ms): initialization with configuration time
- `build-time.total` (time in ms): total build time

### Tasks metrics

- `tasks.executed.<module path>.<task name>.total` (time in ms): top slowest tasks execution time
- `tasks.from_cache.miss` (gauge 0..100): executed tasks ratio.
This is not a real cache misses. See a bug MBS-7244.

### Specific build events

- `app-build.<module path>.<task name>.finish` (time in ms): elapsed time from build start till Android app build task finished
