import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite

class PrettyPrintFiles (
    subprocessor: Processor
) : Passthrough(
    subprocessor,
) {

    override fun backupProcess(process: ProcessingProcess) {
        subprocessor.backupProcess(process)
    }

    override fun backupFolder(folder: ProcessingFile) {
        val terminal = folder.process!!.terminal!!

        val relativePath = subprocessor.relative(folder.sourcePath!!,
            subprocessor.canonical(folder.process!!.profile!!.sourcePath!!))
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
        if (success == false) {
            terminal.println((brightRed)("   ($description)"))
        }
        // Under no scenario can this happen.
        if (success == null)
            terminal.println((brightYellow)("   ($description)"))
    }

    override fun backupFile(file: ProcessingFile) {
        val terminal = file.process!!.terminal!!

        val relativePath = subprocessor.relative(file.sourcePath!!,
            subprocessor.canonical(file.process!!.profile!!.sourcePath!!))
        val size = file.size!!
        terminal.println(Markdown("""
            * ${(brightWhite)(relativePath)} (${(brightWhite)(suffixedSize(size))}) a regular file 
        """.trimIndent()))
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
    }

}