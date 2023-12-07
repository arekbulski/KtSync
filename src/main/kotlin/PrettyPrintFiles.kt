import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
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

        terminal.println(Markdown("""
            * ${(brightWhite)(folder.sourcePath!!)} a folder 
        """.trimIndent()))

        subprocessor.backupFolder(folder)

        // TODO: This wont work correctly in other, failure cases.
        terminal.println("${(brightGreen)("(done)")}")
    }

    override fun backupFile(file: ProcessingFile) {
        val terminal = file.process!!.terminal!!

        terminal.println(Markdown("""
            * ${(brightWhite)(file.sourcePath!!)} (size unknown) a regular file 
        """.trimIndent()))
    }

}