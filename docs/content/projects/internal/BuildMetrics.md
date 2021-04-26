# Build metrics

???+ warning
    This plugin is internal and not recommended to use.  
    See alternatives: 
    [Gradle Enterprise](https://gradle.com/gradle-enterprise-solution-overview/), 
    [Talaiot](https://github.com/cdsap/Talaiot)

## Configuring

### Disabling plugin

Project property `avito.build.metrics.enabled=false`

## Metrics

All metrics can use common placeholders in prefix:

- Namespace: statsd prefix from `avito.stats.namespace` property
- Environment: `ci` | `local` | `mirakle` | `_` (unknown)
- Node: git username for local builds, hostname for CI builds
- Build status: `success` | `failure`

They will be referred in docs as `<placeholder>`.

### Build cache metrics

[Http build cache](https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_configure_remote) errors:

- `<namespace>.build.cache.errors.[load|store].<http status code>`: errors counter

### Common build metrics

Prefix: `<namespace>.<environment>.<node>.id.<build status>`

- `init_configuration.total` (time in ms): initialization with configuration time
- `build-time.total` (time in ms): total build time

### Tasks metrics

Prefix: `<namespace>.<environment>.<node>.id.<build status>.tasks`

- `executed.<module path>.<task name>.total` (time in ms): top slowest tasks execution time
- `from_cache.miss` (gauge 0..100): executed tasks ratio.
This is not a real cache misses. See a bug MBS-7244.

### Specific build events

Prefix: `<namespace>.<environment>.<node>.id.<build status>`

- `app-build.<module path>.<task name>.finish` (time in ms): elapsed time from build start till Android app build task finished
