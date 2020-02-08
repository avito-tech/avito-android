---
title: Introduction
type: docs
---

# Android in Avito

Multiple projects live in two repositories:

- Open Source repository (https://github.com/avito-tech/avito-android) - monorepo of all tooling to continuously test and deliver apps to users
- Internal repository (closed source) - monorepo of all apps and shared libraries

Why do we do open source?

- To get more feedback. We need outside perspective
- To make the code easier to change through reusing
- To share knowledge and solutions and make Android development better
- To understand better through explanation
- To supplement our presentations and articles by real production code
- To make it easier to provide reproducible samples for bugs in external libraries 

## Open Source repository

Contacts: [Telegram chat (Russian)](https://t.me/avito_android_opensource)

All source code lives in `subprojects/`:

- `android-test` - code that goes in androidTestImplementation configuration and runs on emulators
- `gradle` - gradle plugins and buildscript dependencies
- `common` - shared code between android-test and gradle

More details - [project structure]({{< ref "/Infrastructure.md" >}})

Besides that modules you will see:

- `ci/` and `/*.sh` - we follow [IaC](https://en.wikipedia.org/wiki/Infrastructure_as_code) principle whenever possible. 
You can see docker images we use to abstract configuration of apps building and testing, as well as testing github project itself.
- `docs/` - documentation you see right now and code to deploy it automatically.\
[How we document]({{< ref "/contributing/Docs.md" >}})
{{< hint warning>}}
You could see links to private resources that are not available for non-employees (links.k.avito.ru).\
It has been done on purpose to have single documentation and show the whole picture.
{{< /hint >}}

## Closed source internal repository

- [How to start]({{< ref "/contributing/HowToStart.md" >}})

Single slack channel for any android related stuff: [#android-dev](http://links.k.avito.ru/slackandroiddev)
