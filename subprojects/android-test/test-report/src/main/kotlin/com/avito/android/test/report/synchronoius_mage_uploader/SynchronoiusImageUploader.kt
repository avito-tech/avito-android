import android.annotation.SuppressLint
import com.avito.filestorage.FutureValue
import com.avito.filestorage.RemoteStorage
import com.avito.logger.Logger
import java.io.File

@SuppressLint("LogNotTimber")
interface SynchronoiusImageUploader {

    fun upload(referenceImage: File): String

    class Impl(
        val remoteStorage: RemoteStorage,
        val logger: Logger
    ) : SynchronoiusImageUploader {

        override fun upload(referenceImage: File): String {
            logger.debug("Started screenshot uploading ${referenceImage?.absolutePath}")
            val referenceImageFuture = if (referenceImage != null) remoteStorage.upload(
                uploadRequest = RemoteStorage.Request.FileRequest.Image(
                    file = referenceImage
                ),
                comment = "reference image"
            ) else null
            logger.debug("Wait for upload")
            return waitUploads(
                referenceImageFuture = referenceImageFuture
            )
        }

        private fun waitUploads(
            referenceImageFuture: FutureValue<RemoteStorage.Result>?
        ): String {
            logger.debug("get referenceImageResult")
            val referenceImageResult = referenceImageFuture?.get()
            if (referenceImageResult is RemoteStorage.Result.Success) {
                logger.debug("return url ${referenceImageResult.url}")
                return referenceImageResult.url
            }
            logger.debug("return url empty string $referenceImageResult")
            return ""
        }
    }
}