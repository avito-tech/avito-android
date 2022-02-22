package ru.avito.image_builder.internal.docker

import ru.avito.image_builder.internal.process.CommandExecutor
import java.time.Duration

internal open class CliDocker(
    private val executor: CommandExecutor = CommandExecutor()
) : Docker {

    override fun build(
        vararg args: String,
        timeout: Duration
    ): Result<ImageId> {
        val successMessagePattern = "^Successfully built (.{12})$".toRegex(RegexOption.MULTILINE)

        return execute("docker", "build", *args, timeout = timeout)
            .mapCatching { output ->
                val result = checkNotNull(successMessagePattern.find(output)?.groups?.get(1)) {
                    """
                        Couldn't find built result by pattern $successMessagePattern
                        Output:
                        $output
                        """.trimIndent()
                }
                ImageId(result.value)
            }
    }

    override fun run(
        vararg args: String,
        timeout: Duration
    ): Result<String> =
        execute("docker", "run", *args, timeout = timeout)

    override fun exec(
        vararg args: String,
        timeout: Duration
    ): Result<String> =
        execute("docker", "exec", *args, timeout = timeout)

    override fun login(
        username: String,
        password: String,
        registry: String?,
        timeout: Duration
    ): Result<Unit> =
        // TODO: try --password-stdin. It might be a problem with non-interactive TTY in CI
        execute("docker", "login", "-u", username, "-p", password, registry.orEmpty(),
            timeout = timeout
        ).map { }

    override fun tag(source: String, target: String, timeout: Duration): Result<Unit> =
        execute("docker", "tag", source, target, timeout = timeout)
            .map { }

    override fun push(vararg args: String, timeout: Duration): Result<Unit> =
        execute("docker", "push", *args, timeout = timeout)
            .map { }

    override fun commit(
        change: String,
        container: String,
        timeout: Duration
    ): Result<ImageId> =
        execute("docker", "commit", "--change", change, container, timeout = timeout)
            .mapCatching { output ->
                check(output.trim().startsWith("sha256:")) {
                    "Unexpected output of docker commit: $output"
                }
                ImageId(
                    value = output.trim().substringAfter("sha256:")
                )
            }

    override fun remove(
        vararg options: String,
        container: String,
        timeout: Duration
    ): Result<Unit> =
        execute("docker", "rm", *options, container, timeout = timeout)
            .map { }

    private fun execute(
        vararg args: String,
        timeout: Duration
    ): Result<String> =
        executor.run(args.toList(), timeout)
}
