import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import com.github.ajalt.mordant.rendering.TextColors.brightMagenta

// This class pretty prints the beginning, progress bar updates, and finalizations of file/folder operations.
class PrettyPrintFiles (subprocessor: Processor) : Passthrough(subprocessor) {

    override fun initFolderProgress(folder: ProcessingFile) {
        val process = folder.process!!
        val profile = process.profile!!
        val terminal = process.terminal!!

        val relativePath = subprocessor.relative(folder.sourcePath!!, subprocessor.absolute(profile.sourcePath!!))
        terminal.println(Markdown("""
            * ${(brightWhite)(relativePath)} a folder 
        """.trimIndent()))
    }

    override fun finishFolderProgress(folder: ProcessingFile, result: Exception?) {
        val process = folder.process!!
        val terminal = process.terminal!!

        when (result) {
            null -> {
                terminal.println((brightGreen)("   (only created)"))
            }
            is TotalFailureException -> {
                terminal.println((brightRed)("   ($result)"))
            }
            is PartialFailureException -> {
                terminal.println((brightYellow)("   ($result)"))
            }
            else -> {
                terminal.println((brightRed)("   ($result)"))
            }
        }

        terminal.println((brightMagenta)("progress is ${process.processedCount} ${suffixedFileSize(process.processedBytes)} out of ${process.estimatedCount} ${suffixedFileSize(process.estimatedBytes)}"))
    }

    override fun initFileProgress(file: ProcessingFile) {
        val process = file.process!!
        val profile = process.profile!!
        val terminal = process.terminal!!

        val relativePath = subprocessor.relative(file.sourcePath!!, subprocessor.absolute(profile.sourcePath!!))
        if (file.isRegularFile) {
            terminal.println(Markdown("""
                * ${(brightWhite)(relativePath)} (${(brightWhite)(suffixedFileSize(file.size))}) a regular file 
            """.trimIndent()))
        } else {
            terminal.println(Markdown("""
                * ${(brightWhite)(relativePath)} unknown type
            """.trimIndent()))
        }
        val progressbar = terminal.progressAnimation {
            progressBar()
            completed(suffix = "B", includeTotal = true)
        }
        progressbar.start()
        progressbar.update(process.processedBytes, process.estimatedBytes)
        process.progressbar = progressbar
    }

    override fun updateFileProgress(file: ProcessingFile, progress: Long) {
        val process = file.process!!
        val progressbar = process.progressbar!!

        progressbar.update(progress)
    }

    override fun finishFileProgress(file: ProcessingFile, result: Exception?) {
        val process = file.process!!
        val terminal = process.terminal!!
        val progressbar = process.progressbar!!

        progressbar.clear()
        process.progressbar = null

        when (result) {
            null -> {
                terminal.println((brightGreen)("   (done)"))
            }
            is TotalFailureException -> {
                terminal.println((brightRed)("   ($result)"))
            }
            is PartialFailureException -> {
                terminal.println((brightYellow)("   ($result)"))
            }
            else -> {
                terminal.println((brightRed)("   ($result)"))
            }
        }

        terminal.println((brightMagenta)("progress is ${process.processedCount} ${suffixedFileSize(process.processedBytes)} out of ${process.estimatedCount} ${suffixedFileSize(process.estimatedBytes)}"))
    }

    override fun initSymbolicLinkProgress(symlink: ProcessingFile) {
        val process = symlink.process!!
        val profile = process.profile!!
        val terminal = process.terminal!!

        val relativePath = subprocessor.relative(symlink.sourcePath!!, subprocessor.absolute(profile.sourcePath!!))
        terminal.println(Markdown("""
            * ${(brightWhite)(relativePath)} a symbolic link
        """.trimIndent()))
    }

    override fun finishSymbolicLinkProgress(symlink: ProcessingFile, result: Exception?) {
        val process = symlink.process!!
        val terminal = process.terminal!!

        when (result) {
            null -> {
                terminal.println((brightGreen)("   (done)"))
            }
            is TotalFailureException -> {
                terminal.println((brightRed)("   ($result)"))
            }
            is PartialFailureException -> {
                terminal.println((brightYellow)("   ($result)"))
            }
            else -> {
                terminal.println((brightRed)("   ($result)"))
            }
        }

        terminal.println((brightMagenta)("progress is ${process.processedCount} ${suffixedFileSize(process.processedBytes)} out of ${process.estimatedCount} ${suffixedFileSize(process.estimatedBytes)}"))
    }

    override fun initEstimationProgress(process: ProcessingJob) {
        val terminal = process.terminal!!

        terminal.println(Markdown("""
            Indexing source folder for total amount of files and bytes to backup...
        """.trimIndent()))
        val progressbar = terminal.progressAnimation {
            progressBar()
            completed(suffix = " files", includeTotal = false)
        }
        progressbar.start()
        process.progressbar = progressbar
    }

    override fun updateEstimationProgress(process: ProcessingJob) {
        val terminal = process.terminal!!
        val progressbar = process.progressbar!!

        progressbar.update(process.estimatedCount)
    }

    override fun finishEstimationProgress(process: ProcessingJob) {
        val terminal = process.terminal!!
        val progressbar = process.progressbar!!

        progressbar.clear()
        process.progressbar = null
        terminal.println("Found ${(brightWhite)("${process.estimatedCount} files")} totaling ${(brightWhite)(suffixedFileSize(process.estimatedBytes))}.")
        terminal.println()
    }

}