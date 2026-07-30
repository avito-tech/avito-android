package com.avito.android.test.report.video

import android.os.Build
import com.avito.android.Result
import com.avito.android.test.report.video.VideoCaptureMetrics.Reason
import com.avito.android.waiter.waitFor
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.TestArtifactsProvider
import com.avito.time.TimeProvider
import java.io.File
import java.util.concurrent.TimeUnit

public interface VideoCapturer {

    public fun start(): Result<Unit>

    public fun stop(): Result<File>

    public fun abort()
}

internal class VideoCapturerImpl(
    private val testArtifactsProvider: TestArtifactsProvider,
    private val appCacheDir: File,
    private val shellCommandExecutor: ShellCommandExecutor,
    private val videoValidator: VideoValidator,
    private val metrics: VideoCaptureMetrics,
    private val timeProvider: TimeProvider,
    private val sdkInt: Int,
    loggerFactory: LoggerFactory
) : VideoCapturer {

    private val logger = loggerFactory.create<VideoCapturer>()

    private var state: State = State.Idling

    @Synchronized
    override fun start(): Result<Unit> {
        // checks if execute start() concurrently
        return Result.tryCatch {
            require(state is State.Idling) {
                "Can't start video capturing. Capturer isn't Idling"
            }
        }.flatMap {
            testArtifactsProvider.generateUniqueFile("mp4")
                .rescue { failure -> Result.Failure(IllegalStateException("Can't create video file", failure)) }
                .flatMap { videoFile ->
                    testArtifactsProvider.generateFile("video-output", "txt", create = true)
                        .rescue { failure ->
                            Result.Failure(IllegalStateException("Can't create video output file", failure))
                        }
                        .flatMap { recorderLogFile ->
                            Result.tryCatch {
                                executeRecorderCommand("start $videoFile", recorderLogFile)
                                state = State.Recording(videoFile, recorderLogFile)
                            }.rescue { failure ->
                                Result.Failure(IllegalStateException("Can't start video capturing", failure))
                            }
                        }
                }
        }.map { /*Unit*/ }
    }

    @Synchronized
    override fun stop(): Result<File> {
        val startedAt = timeProvider.nowInMillis()
        return when (val castHelperLocalState = state) {
            is State.Recording -> {
                val (videoFile, recorderLogFile) = castHelperLocalState
                val result = when (val saved = saveVideo(videoFile, recorderLogFile)) {
                    is SaveResult.Saved -> {
                        metrics.onStopSuccess(durationSince(startedAt))
                        Result.Success(saved.video)
                    }
                    is SaveResult.Failed -> {
                        metrics.onStopError(saved.reason, durationSince(startedAt))
                        if (videoFile.exists()) {
                            videoFile.delete()
                        }
                        val recorderLog = if (recorderLogFile.exists()) {
                            recorderLogFile.readText().also {
                                recorderLogFile.delete()
                            }
                        } else {
                            "empty"
                        }
                        Result.Failure(
                            IllegalStateException(
                                "Failed when stopping video record. Output: $recorderLog",
                                saved.error
                            )
                        )
                    }
                }
                this.state = State.Idling
                result
            }
            else -> {
                metrics.onStopError(Reason.NOT_RECORDING, durationSince(startedAt))
                Result.Failure(IllegalStateException("Can't stop video capturing. Capturer isn't recording"))
            }
        }
    }

    private fun saveVideo(video: File, recorderLogFile: File): SaveResult =
        try {
            executeRecorderCommand("stop", recorderLogFile)
            SaveResult.Saved(awaitSavedVideo(video))
        } catch (e: VideoIsNotReadableException) {
            SaveResult.Failed(Reason.NOT_READABLE, e)
        } catch (e: VideoCopyFailedException) {
            SaveResult.Failed(Reason.COPY_FAILED, e)
        } catch (t: Throwable) {
            SaveResult.Failed(Reason.SHELL_FAILED, t)
        }

    private fun durationSince(startedAt: Long): Long = timeProvider.nowInMillis() - startedAt

    @Synchronized
    override fun abort() {
        when (val castHelperLocalState = state) {
            is State.Recording -> {
                val (videoFile, recorderLogFile) = castHelperLocalState

                try {
                    executeRecorderCommand("abort", recorderLogFile)
                } catch (t: Throwable) {
                    logger.warn("Can't abort capture", t)
                } finally {
                    if (videoFile.exists()) {
                        videoFile.delete()
                    }
                    this.state = State.Idling
                }
            }
            State.Idling -> {
                // do nothing
            }
        }
    }

    private fun executeRecorderCommand(command: String, recorderLogFile: File) {
        val recordingScript = createRecorderBinary().getOrThrow()
        shellCommandExecutor.execute("sh $recordingScript $command", recorderLogFile)
    }

    private fun createRecorderBinary() = testArtifactsProvider
        .provideReportDir().map { reportDir ->
            val binary = File(
                reportDir,
                RECORDER_BINARY_NAME
            )
            binary.createOrClear()
            binary.writeText(RECORDER_BINARY_CONTENT)
            binary.absolutePath
        }

    private fun File.createOrClear() {
        if (exists()) {
            writer().use { it.write("") }
        } else {
            parentFile?.mkdirs()
            createNewFile()
        }
    }

    /**
     * screenrecord writes the file asynchronously, so we wait until the video becomes readable.
     *
     * Before Android 16 the app reads a shell-written file directly, so we keep the old behavior
     * and don't pay for copying. Since Android 16 direct reading is denied, see [copyToAppStorage].
     */
    private fun awaitSavedVideo(video: File): File {
        val needsCopy = sdkInt >= Build.VERSION_CODES.BAKLAVA
        val readableVideo = if (needsCopy) File(appVideoDir(), video.name) else video
        waitFor(
            timeoutMs = TimeUnit.SECONDS.toMillis(2),
            frequencyMs = 200,
            allowedExceptions = setOf(VideoIsNotReadableException::class.java)
        ) {
            if (needsCopy) {
                copyToAppStorage(video, readableVideo)
            }

            if (!videoValidator.isReadable(readableVideo)) {
                throw VideoIsNotReadableException(readableVideo)
            }
        }
        return readableVideo
    }

    /**
     * Reads the video by shell instead of reading the file from the app process.
     *
     * screenrecord runs as the shell uid, so com.android.shell owns the MediaStore row.
     * Since Android 16 the ENABLE_OWNED_PHOTOS compat change (310703690, enabled for
     * targetSdk >= 36) denies reading such videos even in the app's own Android/media/<pkg>:
     * MediaProvider requires READ_MEDIA_VIDEO and opening the file fails with EACCES
     * ("Permission to access file ... is denied" in logcat).
     *
     * That's why the file is read by its creator: `cat` output comes to a pipe, the app reads it
     * to the end and stores it in the internal storage. The copy is available to the app without
     * MediaProvider on any api and regardless of the compat change. The original file is left
     * untouched, it remains a test artifact on the device.
     */
    private fun copyToAppStorage(video: File, copy: File) {
        try {
            shellCommandExecutor.execute("cat ${video.absolutePath}").use { input ->
                copy.outputStream().use { target -> input.copyTo(target) }
            }
        } catch (t: Throwable) {
            throw VideoCopyFailedException(video, copy, t)
        }
    }

    private fun appVideoDir(): File = File(appCacheDir, VIDEO_DIR_NAME).apply { mkdirs() }

    private sealed interface SaveResult {

        data class Saved(val video: File) : SaveResult

        data class Failed(val reason: Reason, val error: Throwable) : SaveResult
    }

    private sealed class State {

        data object Idling : State()

        data class Recording(
            val video: File,
            val recorderLogFile: File
        ) : State()
    }
}

private const val TAG = "VideoCapturer"

private const val RECORDER_BINARY_NAME = "recorder"

/**
 * Directory in the app internal storage for video copies, see [VideoCapturerImpl.copyToAppStorage]
 */
private const val VIDEO_DIR_NAME = "video"

/**
 * Зачем это тут?
 *
 * Мы запускаем запись видео от имени UiAutomation сервиса. У нашего процесса нет столько прав.
 * Это накладывает некоторые ограничения. У нас вообще нету возможности привязаться к процессам,
 * которые запущенны удаленно (например, элементарно узнать, что они завершились).
 *
 * Поэтому принял решение сделать обертку вокруг screenrecord с красивым API, в котором будет атомарно происходить вся
 * логика, связанная с записью и остановкой видео.
 *
 * А сам баш скрипт лежит тут строкой, т.к таким образом его удобно деливерить на девайс.
 *
 * Вот так.
 */
@Suppress("MaxLineLength")
private const val RECORDER_BINARY_CONTENT = """
logging() {
    echo ${'$'}1
    log -t $TAG ${'$'}1
}

sdk() {
    getprop ro.build.version.sdk
}

kill_process() {
    local signal=${'$'}1
    [ -z "${'$'}{signal}" ] && logging 'You must provide signal number argument for kill_process function' && return 1
    shift

    local process_name=${'$'}1
    [ -z "${'$'}{process_name}" ] && logging 'You must provide process name as second argument for kill_process function' && return 1\
    shift

    local sdk_version=`sdk`
    [ -z "${'$'}{sdk_version}" ] && logging 'Unable to get sdk version' && return 1

    logging "Sdk version is: ${'$'}{sdk_version}"

    if [ ${'$'}{sdk_version} -eq 23 ]; then
        pkill -${'$'}{signal} ${'$'}{process_name}
    else
        pkill -l${'$'}{signal} ${'$'}{process_name}
    fi
}

kill_screen_record() {
    local signal=${'$'}1
    [ -z "${'$'}{signal}" ] && logging 'You must provide signal number argument for kill_screen_record function' && return 1

    kill_process ${'$'}{signal} screenrecord
    local success=${'$'}?

    if [ ${'$'}{success} -eq 0 ]; then
        logging 'Screenrecord process killed successfully'
    else
        logging 'Unable to kill screenrecord process. Possibly it has not started yet'
    fi
}

stop() {
    kill_screen_record 2
}

abort() {
    kill_screen_record 9
}

start() {
    abort || true

    local video_path=${'$'}1
    [ -z "${'$'}{video_path}" ] && logging 'You must provide video path argument for start command' && return 1

    shift

    logging "Recording video to file ${'$'}{video_path}..."
    screenrecord --verbose ${'$'}{video_path} ${'$'}@
}

command="${'$'}1"

[ -z "${'$'}{command}" ] && logging 'Command argument has not passed' && return 1

shift

case ${'$'}{command} in
start)
    start ${'$'}@
    exit 0
    ;;
stop)
    stop ${'$'}@
    exit 0
    ;;
abort)
    abort ${'$'}@
    exit 0
    ;;
esac
"""

internal class VideoIsNotReadableException(video: File) :
    RuntimeException("Unable to get frame from video ${video.absolutePath}")

internal class VideoCopyFailedException(video: File, copy: File, cause: Throwable) :
    RuntimeException("Unable to copy video ${video.absolutePath} to ${copy.absolutePath}", cause)
