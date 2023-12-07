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
            ## Preface
            KtSync is starting to backup your files. You chose to backup the folder ${(brightWhite)(process.profile!!.sourcePath!!)} into the folder ${(brightWhite)(process.profile!!.destinationPath!!)} using the ${(brightWhite)("Full Backup algorithm")}. If the destination already exists, it will be safely renamed, do not worry about that.
            ## Progress
        """.trimIndent()))

        passthrough({
            subprocessor.backupProcess(process)

            // TODO: Print next section?
        }, {
            terminal.println(Markdown("""
                ## Summary
                ${(brightGreen)("All done. Everything, ${(brightWhite)("${process.successfulEntries} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))}, was backed up.")}
            """.trimIndent()))
        }, {
            terminal.println(Markdown("""
                ## Summary
                ${(brightYellow)("Backup has partially failed. ${(brightWhite)("${process.successfulEntries} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))} were successfully backed up, however ${(brightWhite)("${process.failedEntries} files/folders")} were not backed up.")}
            """.trimIndent()))
            throw it
        }, {
            terminal.println(Markdown("""
                ## Summary
                ${(brightRed)("Backup has entirely failed due to $it. Destination folder is in indeterminate state.")}
            """.trimIndent()))
            throw it
        }, {
            terminal.println(Markdown("""
                ## Summary
                ${(brightRed)("Backup has entirely failed due to unknown exception $it. Destination folder is in indeterminate state.")}
            """.trimIndent()))
            throw it
        })
    }

}