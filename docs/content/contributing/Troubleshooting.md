# Troubleshooting

## Configuration cache opt-out

Experimental configuration cache support enabled by default for local work. 
If something goes wrong with it and you have no time to fix it, you can add:

`~/.gradle/gradle.properties`

```properties
org.gradle.unsafe.configuration-cache=false
```

**Why not opt-in?**

Avito project has configuration cache disabled, just not all issues fixed for now.  
Enabling this flag will enable it for all other gradle projects.

Alternative will be adding `--configuration-cache` to `Makefile` targets and IDE Run configurations, 
which is somewhat hard to maintain.
