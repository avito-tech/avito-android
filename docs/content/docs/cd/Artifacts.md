---
title: Artifacts
type: docs
---

# Артефакты сборки (internal)

## Где храним артефакты?

Используем несколько хранилищ:

- [Artifactory](http://links.k.avito.ru/artifactoryAppsReleaseLocal)
    - feature-toggles.json
    - api.json
    - staging apk
- [QApps]({{< relref "QApps.md" >}})
- [Play market/Play console](https://play.google.com/apps/publish/)
    - релизный aab
    - релизный proguard mapping
- [Teamcity Android PR checks](http://links.k.avito.ru/tmctAvitoAndroidBuild)
    - Вкладка `Artifacts` у билда
    - Все файлы pull request
    - Живет ~ 1 месяц
- [Teamcity Android Release](http://links.k.avito.ru/tmctAvitoAndroidDeployToPlayContract)
    - Вкладка `Artifacts` у билда
    - Все файлы релизной сборки
    - Живет ~ 1 месяц
