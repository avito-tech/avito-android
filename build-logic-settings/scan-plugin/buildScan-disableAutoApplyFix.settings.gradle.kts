/**
 * Workaround for auto-applied build scan plugin - `--scan` argument.
 * In this case the plugin is applied implicitly and before our convention plugins.
 * In some cases it can't find plugin itself:
 * ```
 * Where:
 * Auto-applied by using --scan
 * What went wrong:
 * Could not apply requested plugin [id: 'com.gradle.enterprise', version: '3.8.1', artifact: 'com.gradle:gradle-enterprise-gradle-plugin:3.8.1'] as it does not provide a plugin with id 'com.gradle.enterprise'.
 * This is caused by an incorrect plugin implementation. Please contact the plugin author(s).
 * > Plugin with id 'com.gradle.enterprise' not found.
 * ```
 *
 * Didn't find reasons.
 * See a discussion here: https://gradle-community.slack.com/archives/CAH4ZP3GX/p1649845394620019?thread_ts=1649842582.572229&cid=CAH4ZP3GX
 *
 * To avoid such imlicit and fragile mutations at all we apply and configure buildScan manually.
 */
pluginManagement {
    if (gradle.startParameter.isBuildScan) {
        gradle.startParameter.isBuildScan = false
        // It will be read in a convention-enterprise plugin
        settings.extra["avito.gradle.buildScan.publish"] = true
    }
}
