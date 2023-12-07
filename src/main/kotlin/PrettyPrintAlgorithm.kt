import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite

class PrettyPrintAlgorithm (
    subprocessor: Processor
) : Passthrough(subprocessor) {

    override fun backupProcess(process: ProcessingProcess) {
        val terminal = process.terminal!!

        terminal.println(Markdown("""
            ## Debug
            Beginning a test run (a hard-coded operation).
            $process
            
        """.trimIndent()))

        passthrough({
            subprocessor.backupProcess(process)

            // TODO: Print next section?
        }, {
            terminal.println((brightGreen)("All done. Everything, ${(brightWhite)("${process.successfulEntries} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))}, was backed up."))
        }, {
            terminal.println((brightYellow)("Backup has partially failed. ${(brightWhite)("${process.successfulEntries} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))} were successfully backed up, however ${(brightWhite)("${process.failedEntries} files/folders")} were not backed up."))
            throw it
        }, {
            terminal.println((brightRed)("Backup has entirely failed due to $it. Destination folder is in indeterminate state."))
            throw it
        }, {
            terminal.println((brightRed)("Backup has entirely failed due to unknown exception $it. Destination folder is in indeterminate state."))
            throw it
        })
    }

    // TODO: TBR
    override fun backupFolder(folder: ProcessingFile) {
        subprocessor.backupFolder((folder))
    }

    // TODO: TBR
    override fun backupFile(file: ProcessingFile) {
        subprocessor.backupFile(file)
    }

}