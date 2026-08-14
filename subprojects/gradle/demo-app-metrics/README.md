# Demo app metrics

The `generateModuleGraph` task writes `build/module-graph.json` with module
dependency analysis:

- `dependencies` contains internal module graph edges.
- `applications` contains sorted `DemoApp` and `UserApp` module paths.
- `linesOfCode` contains each module's own `main` LOC. `test` and `androidTest`
  are present only for application modules.
- `transitiveLinesOfCode` contains LOC for each module and its dependencies.
- `impactedApplications` maps each module to applications affected by it.
