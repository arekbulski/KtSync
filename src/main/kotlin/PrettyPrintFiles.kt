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

        val relativePath = subprocessor.relative(folder.sourcePath!!,
            subprocessor.canonical(folder.process!!.profile!!.sourcePath!!))
        terminal.println(Markdown("""
            * ${(brightWhite)(relativePath)} a folder 
        """.trimIndent()))

        subprocessor.backupFolder(folder)

        // TODO: This wont work correctly in other, failure cases.
        terminal.println("${(brightGreen)("(done)")}")
    }

    override fun backupFile(file: ProcessingFile) {
        val terminal = file.process!!.terminal!!

        val relativePath = subprocessor.relative(file.sourcePath!!,
            subprocessor.canonical(file.process!!.profile!!.sourcePath!!))
        terminal.println(Markdown("""
            * ${(brightWhite)(relativePath)} (size unknown) a regular file 
        """.trimIndent()))
    }

}