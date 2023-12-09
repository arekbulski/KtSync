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
        val profile = process.profile!!
        val terminal = process.terminal!!

        when (result) {
            null -> {
                terminal.println((brightGreen)("   (created)"))
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

        terminal.println((brightMagenta)("progress is ${process.processedCount} ${suffixedSize(process.processedBytes)} out of ${process.estimatedCount} ${suffixedSize(process.estimatedBytes)}"))
    }

    override fun initFileProgress(file: ProcessingFile) {
        val process = file.process!!
        val terminal = process.terminal!!

        val relativePath = subprocessor.relative(file.sourcePath!!,
            subprocessor.absolute(file.process!!.profile!!.sourcePath!!))
        if (file.isRegularFile) {
            terminal.println(Markdown("""
                * ${(brightWhite)(relativePath)} (${(brightWhite)(suffixedSize(file.size))}) a regular file 
            """.trimIndent()))
            val progressbar = terminal.progressAnimation {
                progressBar()
                completed(includeTotal = true)
                speed("bytes/sec")
                timeRemaining()
            }
            progressbar.start()
            progressbar.update(process.processedBytes, process.estimatedBytes)
            process.progressbar = progressbar
        } else {
            terminal.println(Markdown("""
                * ${(brightWhite)(relativePath)} unknown type
            """.trimIndent()))
        }

    }

    override fun finishFileProgress(file: ProcessingFile, result: Exception?) {
        val process = file.process!!
        val terminal = process.terminal!!
        val progressbar = process.progressbar!!

        // TODO: Displays fake progress over 1 second.
        repeat(10) {
            Thread.sleep(100)
            progressbar.update()
        }

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

        terminal.println((brightMagenta)("progress is ${process.processedCount} ${suffixedSize(process.processedBytes)} out of ${process.estimatedCount} ${suffixedSize(process.estimatedBytes)}"))
    }

    override fun initEstimationProgress(process: ProcessingProcess) {
        val terminal = process.terminal!!

        terminal.println(Markdown("""
            Estimating a total amount of files and bytes to backup...
        """.trimIndent()))
        val progressbar = terminal.progressAnimation {
            progressBar()
            completed(suffix = " files", includeTotal = false)
        }
        progressbar.start()
        process.progressbar = progressbar
    }

    override fun updateEstimationProgress(process: ProcessingProcess) {
        val terminal = process.terminal!!
        val progressbar = process.progressbar!!

        progressbar.update(process.estimatedCount)

        repeat(10) {
            Thread.sleep(25)
            progressbar.update()
        }
    }

    override fun finishEstimationProgress(process: ProcessingProcess) {
        val terminal = process.terminal!!
        val progressbar = process.progressbar!!

        progressbar.clear()
        process.progressbar = null
        terminal.println("Found ${(brightWhite)("${process.estimatedCount} files")} totaling ${(brightWhite)(suffixedSize(process.estimatedBytes))}.")
        terminal.println()
    }

}