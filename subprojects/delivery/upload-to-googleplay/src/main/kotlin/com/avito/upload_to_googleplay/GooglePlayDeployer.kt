package com.avito.upload_to_googleplay

/**
 * Код подсмотрен в https://github.com/Triple-T/gradle-play-publisher
 * https://developers.google.com/android-publisher/api-ref/
 * @see [com.avito.cd.getCdBuildConfig] валидация, которая гарантирует,
 * что с [GooglePlayDeploy.applicationId] ассоциирован один [GooglePlayDeploy]
 */
public interface GooglePlayDeployer {

    public fun deploy(deploys: List<GooglePlayDeploy>)
}
