import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import com.github.ajalt.mordant.rendering.TextColors.brightMagenta

class PrettyPrintFiles (
    subprocessor: Processor
) : Passthrough(subprocessor) {

    override fun backupFolder(folder: ProcessingFile) {
        val terminal = folder.process!!.terminal!!

        val relativePath = subprocessor.relative(folder.sourcePath!!,
            subprocessor.absolute(folder.process!!.profile!!.sourcePath!!))
        terminal.println(Markdown("""
            * ${(brightWhite)(relativePath)} a folder 
        """.trimIndent()))

        subprocessor.backupFolder(folder)
    }

    override fun finishFolder(folder: ProcessingFile, success: Boolean?, description: String?) {
        val terminal = folder.process!!.terminal!!

        if (success == true) {
            terminal.println((brightGreen)("   (created)"))
        }
        // Under no scenario can this happen.
        if (success == null) {
            terminal.println((brightYellow)("   ($description)"))
        }
        if (success == false) {
            terminal.println((brightRed)("   ($description)"))
        }

        val process = folder.process!!

        terminal.println((brightMagenta)("progress is ${process.processedCount} ${suffixedSize(process.processedBytes)} out of ${process.estimatedCount} ${suffixedSize(process.estimatedBytes)}"))
    }

    override fun backupFile(file: ProcessingFile) {
        val terminal = file.process!!.terminal!!

        val relativePath = subprocessor.relative(file.sourcePath!!,
            subprocessor.absolute(file.process!!.profile!!.sourcePath!!))
        if (file.isRegularFile == true) {
            terminal.println(Markdown("""
                * ${(brightWhite)(relativePath)} (${(brightWhite)(suffixedSize(file.size))}) a regular file 
            """.trimIndent()))
        } else {
            terminal.println(Markdown("""
                * ${(brightWhite)(relativePath)} unknown type
            """.trimIndent()))
        }
    }

    override fun finishFile(file: ProcessingFile, success: Boolean?, description: String?) {
        val terminal = file.process!!.terminal!!

        if (success == true) {
            terminal.println((brightGreen)("   (done)"))
        }
        if (success == false) {
            terminal.println((brightRed)("   ($description)"))
        }
        if (success == null)
            terminal.println((brightYellow)("   ($description)"))

        val process = file.process!!

        terminal.println((brightMagenta)("progress is ${process.processedCount} ${suffixedSize(process.processedBytes)} out of ${process.estimatedCount} ${suffixedSize(process.estimatedBytes)}"))
    }

}