# Removal of ui tests impact analysis functionality

Avito had a long standing experiment with impact analysis of ui tests which is come to an end.

Reasons:

- We extracted almost all component tests from main app module and distributed to separate demo apps, which uses
  basic [Impact Analysis](../../../ci/ImpactAnalysis.md) approach to skip unaffected tests on module level
- We don't run e2e tests on PR, which will be, ultimately, only type of tests we can't extract
- Impact analysis for ui tests is fragile, depends on changing links between modules and id's (set manually)
- Resources link broke multiple times on AGP updates and will continue to break with new namespaced resources

So it is just a complex demanding system with almost zero impact on current processes.

[Discussion](https://github.com/avito-tech/avito-android/discussions/688)

## Artifacts

### Code

[2021.4 release tag](https://github.com/avito-tech/avito-android/tree/2021.4):

- `subprojects/gradle/instrumentation-test-impact-analysis`
- `subprojects/gradle/ui-test-bytecode-analyzer`
- `samples/test-app-impact`

### Video

[Dmitriy Merkuriev](https://github.com/dimorinny)'s
talk: [Android CI Impact analysis - AppsConf Mobile meetup (RU)](https://youtu.be/EBO2S9qcp0s?t=6948)

### Docs

Part of the documentation about usage and drawbacks:

```
### Tradeoffs in impact analysis

To bind a Page Object to a Gradle-module we keep this information in the code.
See [Screen.rootId](../test_framework/TestFramework.md#screen)

There are two types of errors in impact analysis:

- False-negative: haven't run affected tests. 
- False-positive: run extra tests.

There is a special case - fallback. If we can't understand impact of changes, we ran all tests.
We loose time in favor of correctness.

Known fallbacks:

- Fallback on test level: if a class has been changed, we'll run all his tests.
- Fallback on module level: if a module has been changed, we'll run all tests from its package.
- Fallback on Screen level: if we can't find Screen's package, we'll run all tests related to it.
    - Screen doesn't match any of packages
    - Screen matches to multiple packages
```
