package com.avito.android.test.report.video

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_WRITE
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.waitFor
import com.avito.android.util.executeMethod
import com.avito.android.util.getFieldValue
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

interface VideoCapturer {
    fun start()
    fun stop(): File?
    fun abort()
}

class VideoCapturerImplementation(
    private val outputDirectory: Lazy<File>
) : VideoCapturer {

    private val state = AtomicReference<State>(State.Idling)

    override fun start() {
        if (state.get() !is State.Idling) {
            throw RuntimeException("Unable to start recording because you're already recording video")
        }

        val uuid = UUID.randomUUID().toString()
        val videoFile = testVideoFile(uuid)
        val outputFile = outputFile()

        executeRecorderCommand("start ${videoFile.absolutePath}", outputFile)

        val recording = State.Recording(videoFile, outputFile)

        // fails if execute start() concurrently
        require(state.compareAndSet(State.Idling, recording)) {
            "Record has been started already while we were starting a $recording"
        }
    }

    @SuppressLint("LogNotTimber")
    override fun stop(): File? {
        val state = state.get()
        if (state !is State.Recording) {
            throw RuntimeException("Unable to stop recording because recording process hasn't started yet")
        }

        val (videoFile, outputFile)  = state

        executeRecorderCommand("stop", outputFile)

        try {
            waitForVideoSaving(videoFile)
        } catch (t: Throwable) {
            if (outputFile.exists()) {
                Log.v(
                    TAG,
                    "Something went wrong during test recording. Output: ${outputFile.readText()}. Stacktrace: ${t}"
                )
            }

            if (videoFile.exists()) {
                videoFile.delete()
            }

            return null
        }

        this.state.compareAndSet(state, State.Idling)

        return videoFile
    }

    override fun abort() {
        val state = state.get()
        if (state is State.Recording) {
            val (videoFile, outputFile) = state

            executeRecorderCommand("abort", outputFile)

            if (videoFile.exists()) {
                videoFile.delete()
            }

            this.state.compareAndSet(state, State.Idling)
        }
    }

    private fun executeRecorderCommand(command: String, output: File) {
        val recorderPath = createRecorderBinary()

        execute("sh $recorderPath $command", output)
    }

    private fun createRecorderBinary(): String {
        val binary = File(
            outputDirectory.value,
            RECORDER_BINARY_NAME
        )

        binary.createOrClear()
        binary.writeText(RECORDER_BINARY_CONTENT)

        return binary.absolutePath
    }

    fun File.createOrClear() {
        if (exists()) {
            writer().use { it.write("") }
        } else {
            parentFile.mkdirs()
            createNewFile()
        }
    }


    private fun testVideoFile(testName: String) = File(
        outputDirectory.value,
        "$testName.mp4"
    )

    private fun outputFile() = File(
        outputDirectory.value,
        "output.txt"
    ).apply {
        createNewFile()
    }

    private fun waitForVideoSaving(video: File) {
        waitFor(
            timeoutMs = TimeUnit.SECONDS.toMillis(2),
            frequencyMs = 200,
            allowedExceptions = setOf(RuntimeException::class.java)
        ) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(video.absolutePath)

            if (retriever.frameAtTime == null) {
                throw RuntimeException("Unable to get frame from video")
            }
        }
    }

    /**
     * Вызываем executeShellCommand напрямую из UiAutomationConnection т.к uiAutomation
     * содержит в себе логику перенаправления output из процесса, который запускается
     * внутри UIAutomation сервиса, в PIPE. Жизненный цикл этих PIPE's привязан к жизненному циклу
     * процесса Instrumentation и из за этого может происходить гонка между процессом, запущенным
     * от имени UiAutomation сервиса (например, запись видео) и проходом тестов.
     *
     * В этом методе мы делаем то же самое что и uiAutomation.executeShellCommand(), но даем возможность
     * прокинуть любой файловый дескриптор (а не только пайп), что позволяет избежать проблемы, описанной выше.
     *
     * https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/android/app/UiAutomationConnection.java
     */
    private fun execute(command: String, output: File) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val connection = automation.getFieldValue<Any>("mUiAutomationConnection")

        val outputDescriptor = ParcelFileDescriptor.open(
            output,
            MODE_READ_WRITE
        )
        val inputDescriptor: ParcelFileDescriptor? = null

        /**
         * Начиная с версии Андроид 27 изменилась сигнатура метода executeShellCommand внутри класса
         * UiAutomationConnection. Появился еще 1 аргумент, который позволяет задать input для запускаемого процесса.
         *
         * Вот так выглядит метод до 27:
         * https://chromium.googlesource.com/android_tools/+/e429db7f48cd615b0b408cda259ffbc17d3945bb/sdk/sources/android-23/android/app/UiAutomationConnection.java#230
         *
         * А вот так после:
         * https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-8.1.0_r14/core/java/android/app/UiAutomationConnection.java#305
         */
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O_MR1) {
            connection.executeMethod(
                "executeShellCommand",
                command,
                outputDescriptor
            )
        } else {
            connection.executeMethod(
                "executeShellCommand",
                command,
                outputDescriptor,
                inputDescriptor
            )
        }
        try {
            outputDescriptor.close()
        } catch (ignore: IOException) {
            //ignore
        }
    }

    private sealed class State {
        object Idling : State()

        data class Recording(
            val video: File,
            val output: File
        ) : State()
    }
}

private const val TAG = "VideoCapturer"

private const val RECORDER_BINARY_NAME = "recorder"

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
