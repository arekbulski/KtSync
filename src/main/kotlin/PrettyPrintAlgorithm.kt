import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed

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
            terminal.println((brightYellow)("Backup has partially failed due to $it."))
            throw it
        }, {
            terminal.println((brightRed)("Backup has entirely failed due to $it."))
            throw it
        }, {
            terminal.println((brightRed)("Backup has entirely failed due to $it."))
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