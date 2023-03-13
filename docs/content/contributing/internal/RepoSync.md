# Synchronization from Stash to GitHub
To perform one-way synchronization use the 
[Repository Mirror Plugin for Bitbucket Server](http://links.k.avito.ru/0PC) fork.
Synchronize the `develop` branch, release branches, and all tags.

## How to configure Mirror Hook
The synchronization plugin is configured in the Hooks tab of the `avito-android-tools` repository settings.

- Mirror Repo URL: `https://github.com/avito-tech/avito-android.git`
- Username: GitHub Username of the token creator
- Password: You need to use fine-grained GitHub token with
  Contents write permissions in one GitHub repo only. Also,
  token user should be in exceptions for ```Allow specific actors to bypass required pull requests```.
  You can request the token from the Speed Android Tech Lead or create it yourself if you have enough GitHub permissions.
- Refspecs: `+refs/heads/develop:refs/heads/develop +refs/heads/20*:refs/heads/20*`
- Tags: `true`
- Notes: `false`
- Atomic: `true`

## How to debug problems
In case of synchronization issues you can get logs through a [task](http://links.k.avito.ru/H5) or the `atlassian` channel in the corporate messenger.
Ask search for ```com.englishtown``` in the ```Stash``` logs.

## How to test the plugin
1. Create a test repository in GitHub
2. Create a test repository in Stash
3. For local testing you can use [BitBucket Server Docker image](https://hub.docker.com/r/atlassian/bitbucket-server)
4. You can also test in [staging Stash](http://links.k.avito.ru/gNe). Just ask admins to install the plugin and restart staging ```Stash```
5. After the plugin is installed, you can configure the synchronization in the Hooks tab of the test repository settings in ```Stash```.
6. Push some changes to the test repository in Stash and check if they are synchronized to the test repository in GitHub

## How to update the plugin
1. Open a [task](http://links.k.avito.ru/H5)
2. Choose ```Stash``` in Service dropdown menu
3. Enter the task title: ```Plugin Update```
4. Describe the problem in the task body
5. Attach the plugin jar file `stash-hook-mirror-*.jar`, which you can obtain by using the command `mvn install`. 
You can find the code at [stash-hook-mirror](http://links.k.avito.ru/0PC). 
Note that update plugin require ```Stash``` restart.
