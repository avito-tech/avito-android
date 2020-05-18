---
title: Test impact analysis
type: docs
---

# Test impact analysis

Test impact analysis finds automatically a minimum set of tests that can verify changed code.

[The Rise of Test Impact Analysis](https://martinfowler.com/articles/rise-test-impact-analysis.html)

## On module level

Besides tests, we have different work to do in modules: Android Lint, unit-tests, assemble, ...\
If a module is not affected by changes, we don't want to run anything in it.

{{<mermaid>}}
graph TD
AppX --> FeatureA
AppX --> FeatureB
AppX --> FeatureC
AppY --> FeatureC
AppY:::changed --> FeatureD:::changed
    
classDef changed fill:#f96;
{{</mermaid>}}

These optimizations are supported in [CI Steps Plugin]({{< ref "/docs/projects/CISteps.md" >}}).\
See implementation in `impact` module.

## UI tests

An overview: [Android CI Impact analysis - AppsConf Mobile meetup (RU)](https://youtu.be/EBO2S9qcp0s?t=6948).

See implementation in `instrumentation-test-impact-analysis` module.

### Tradeoffs in impact analysis

To bind a Page Object to a Gradle-module we keep this information in the code. 
See [Screen.rootId]({{< ref "/docs/test_framework/TestFramework.md#screen" >}})

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

