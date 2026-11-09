# Teamcity build metrics

???+ warning
    This plugin is internal. Contact us if needed

## What metrics does plugin send

Plugin has teamcity `build duration` metric and `queue time` metric 

## How to send metrics

Execute
```shell
./gradlew :subprojects:teamcity-metrics-collector:sendTeamcityBuildsMetrics -PteamcityUrl -PteamcityApiUser -PteamcityApiPassword -PmetricsSourceBuildType -PgraphiteHost -PgraphitePort -PmetricsPrefix
```

Metrics will look like:

- `<-PmetricsPrefix>.teamcity.build;build_type=<-PmetricsSourceBuildType>,status=(success,failure,unknown)`
- `<-PmetricsPrefix>.teamcity.queue;build_type=<-PmetricsSourceBuildType>`

Sources with an `sla` block in the config additionally send two counters per pass, stamped with the current time: `<sla.metricsPrefix>.total` (finished PR builds) and `<sla.metricsPrefix>.bad` (those longer than `sla.thresholdMinutes`).


