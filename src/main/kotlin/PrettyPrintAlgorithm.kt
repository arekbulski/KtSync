import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite

class PrettyPrintAlgorithm (
    val subprocessor: Processor
) : Processor() {

    override fun backupProcess(process: ProcessingProcess) {
        val terminal = process.terminal!!

        terminal.println(Markdown("""
            ## Debug
            Beginning a test run (a hard-coded operation).
        """.trimIndent()))

        passthrough({
            subprocessor.backupProcess(process)
        }, {
            terminal.println((brightGreen)("All done. Everything is backed up."))
        }, {
            terminal.println((brightYellow)("Backup has partially failed. ${(brightWhite)("${process.failedEntries} files")} totaling ${(brightWhite)("${process.failedBytes} bytes")} were not backed up."))
            throw it
        }, {
            terminal.println((brightRed)("Backup has entirely failed due to $it. Destination folder is in indeterminate state."))
            throw it
        }, {
            terminal.println((brightRed)("Backup has entirely failed due to unknown exception $it. Destination folder is in indeterminate state."))
            throw it
        })
    }

    override fun backupFolder(folder: ProcessingFile) {
        subprocessor.backupFolder((folder))
    }

    override fun backupFile(file: ProcessingFile) {
        subprocessor.backupFile(file)
    }

}