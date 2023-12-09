import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import com.github.ajalt.mordant.rendering.TextColors.brightMagenta

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

    // TODO: Change signature to use exception objects.
    override fun finishFolderProgress(folder: ProcessingFile, success: Boolean?, description: String?) {
        val process = folder.process!!
        val profile = process.profile!!
        val terminal = process.terminal!!

        if (success == true) {
            terminal.println((brightGreen)("   (created)"))
        }
        if (success == null) {
            terminal.println((brightYellow)("   ($description)"))
        }
        if (success == false) {
            terminal.println((brightRed)("   ($description)"))
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

    // TODO: Change signature to use exception objects.
    override fun finishFileProgress(file: ProcessingFile, success: Boolean?, description: String?) {
        val process = file.process!!
        val terminal = process.terminal!!
        val progressbar = process.progressbar!!

        repeat(10) {
            Thread.sleep(100)
            progressbar.update()
        }

        progressbar.clear()
        process.progressbar = null

        if (success == true) {
            terminal.println((brightGreen)("   (done)"))
        }
        if (success == null) {
            terminal.println((brightYellow)("   ($description)"))
        }
        if (success == false) {
            terminal.println((brightRed)("   ($description)"))
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