## Docker dir structure conventions

### Hermetic vs Non-Hermetic

Each image should have only `hermetic` or `non-hermetic` dir where place related `Dockerfile`

#### image_name.txt

Each hermetic or non-hermetic dir contains, close to `Dockerfile`, `image_name.txt` with image publication name i.e. `android/builder-hermetic`

## Hermetic containers

These images use only cached in advance dependencies.  
It helps to make containers more hermetic and reproducible.
We can't lock revision of packages by sdkmanager:

- https://issuetracker.google.com/issues/117789774
- https://issuetracker.google.com/issues/38045649

### How to update Android SDK packages

cmdline-tools is a bootstrap and can be downloaded directly from https://developer.android.com/studio#command-tools.

Other packages are downloaded by [sdkmanager](https://developer.android.com/studio/command-line/sdkmanager):

```
# If you do these steps locally, please make any smallest step to automate them.
# The final goal is to run a script in CI.

# To download binaries for container's OS
export REPO_OS_OVERRIDE=linux

# Find a needed dependency
sdkmanager --list

# In case of non Linux, uninstall to clear binaries
sdkmanager --uninstall "<package[;version]>"

# Install a package
sdkmanager --install "<package[;version]>"

# Make a zip file:
- Change structure of directories for easier extraction. See unzip_from_url.sh usages in Dockerfile.
- Add version and OS (if any) to file name. See versions in source.properties file.
- In case of macOS, remove hidden files. They are excessive and causes conflicts while extraction.
zip -d <zip file> "__MACOSX*"
zip -d <zip file> "**/.DS_Store"

# Upload zip to Artifactory
...
```
