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

- `<namespace>.<environment>.<node>.build.cache.errors.[load|store].<http status code>`: errors counter
  
Remote cache statistics:

- `<namespace>.<environment>.<node>.build.cache.remote.[hit|miss]`: remote cache operations count by environments.  
Shows count of cacheable tasks that were requested from the remote cache.  
This is the same as **Performance** | **Build cache** | **Remote cache** | **Operations** | **Hit\Miss** in build scan.

### Common build metrics

- `<namespace>.<environment>.<node>.id.<build status>.init_configuration.total` (time in ms): initialization with configuration time
- `<namespace>.<environment>.<node>.id.<build status>.build-time.total` (time in ms): total build time

### Tasks metrics

- `<namespace>.<environment>.<node>.id.<build status>.tasks.executed.<module path>.<task name>.total` (time in ms): 
  top slowest tasks execution time. It will be removed in MBS-11309.

### Specific build events

- `<namespace>.<environment>.<node>.id.<build status>.app-build.<module path>.<task name>.finish` (time in ms): 
  elapsed time from build start till Android app build task finished
