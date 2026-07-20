package com.avito.android.async_tc_cleaner.internal.reaper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.DirectoryIteratorException
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.SecureDirectoryStream
import java.nio.file.attribute.BasicFileAttributeView

internal class SinglePassTreeDeleter : TreeDeleter {

    override suspend fun delete(stagedDir: StagedDir): DeletionOutcome = withContext(Dispatchers.IO) {
        TreeWalk(coroutineContext.job).deleteEntry(
            parentDirStream = stagedDir.stagingDirStream,
            entryRelativePath = stagedDir.stagedEntryRelativePath,
            parentReportedPath = stagedDir.reportedPath.parent,
        )
    }

    private class TreeWalk(private val job: Job) {

        fun deleteEntry(
            parentDirStream: SecureDirectoryStream<Path>,
            entryRelativePath: Path,
            parentReportedPath: Path?,
        ): DeletionOutcome {
            val attrs = try {
                parentDirStream.getFileAttributeView(
                    entryRelativePath,
                    BasicFileAttributeView::class.java,
                    NOFOLLOW_LINKS
                ).readAttributes()
            } catch (_: NoSuchFileException) {
                return DeletionOutcome.EMPTY
            } catch (e: IOException) {
                return DeletionOutcome.failure(
                    reportedPath = reportedPath(parentReportedPath, entryRelativePath),
                    error = e,
                )
            }

            return if (attrs.isDirectory) {
                deleteDirectory(
                    parentDir = parentDirStream,
                    parentPath = parentReportedPath,
                    directoryName = entryRelativePath,
                )
            } else {
                val sizeBytes = if (attrs.isSymbolicLink) 0L else attrs.size()
                unlink(
                    parentReportedPath, entryRelativePath, sizeBytes
                ) { parentDirStream.deleteFile(entryRelativePath) }
            }
        }

        private fun deleteDirectory(
            parentDir: SecureDirectoryStream<Path>,
            parentPath: Path?,
            directoryName: Path,
        ): DeletionOutcome {
            val directoryStream = try {
                parentDir.newDirectoryStream(directoryName, NOFOLLOW_LINKS)
            } catch (_: NoSuchFileException) {
                return DeletionOutcome.EMPTY
            } catch (e: IOException) {
                return DeletionOutcome.failure(reportedPath(parentPath, directoryName), e)
            }

            val childrenDeletionOutcome = directoryStream.use { dirStream ->
                var outcome = DeletionOutcome.EMPTY
                val directoryReportedPath = reportedPath(parentPath, directoryName)
                try {
                    for (child in dirStream) {
                        job.ensureActive()
                        outcome += deleteEntry(
                            parentDirStream = dirStream,
                            entryRelativePath = child.fileName,
                            parentReportedPath = directoryReportedPath
                        )
                    }
                } catch (e: DirectoryIteratorException) {
                    outcome += DeletionOutcome.failure(
                        reportedPath = directoryReportedPath,
                        error = e.cause ?: e
                    )
                }
                return@use outcome
            }

            val directoryDeletionOutcome = unlink(
                parentReportedPath = parentPath,
                entryRelativePath = directoryName,
                sizeBytes = 0L
            ) {
                parentDir.deleteDirectory(directoryName)
            }
            return childrenDeletionOutcome + directoryDeletionOutcome
        }

        private inline fun unlink(
            parentReportedPath: Path?,
            entryRelativePath: Path,
            sizeBytes: Long,
            remove: () -> Unit,
        ): DeletionOutcome = try {
            remove()
            DeletionOutcome.deleted(sizeBytes)
        } catch (_: NoSuchFileException) {
            DeletionOutcome.EMPTY
        } catch (e: IOException) {
            DeletionOutcome.failure(
                reportedPath = reportedPath(
                    parentPath = parentReportedPath,
                    entryRelativePath = entryRelativePath
                ),
                error = e,
            )
        }
    }
}
