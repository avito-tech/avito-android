# Test Runner sample app

`samples/test-runner`

Includes build of test-runner project for building
as [composite](https://docs.gradle.org/current/userguide/composite_builds.html), so any changes in test-runner will
directly affect sample.

Sync project to get gradle plugin API changes.

## Run

Setup kubernetes context `contextName` in `~/.kube/config`.

To run tests use: `make test_runner_instrumentation kubernetesContext=<contextName>`

## Clean up

If test run stuck, resources in k8s should be freed automatically by cron script (set up in Avito).

For manual cleaning use `kubectl delete deployment -l type=local-<$USER>`
