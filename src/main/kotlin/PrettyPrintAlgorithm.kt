import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import java.time.Duration
import java.time.LocalDateTime

class PrettyPrintAlgorithm (
    subprocessor: Processor
) : Passthrough(subprocessor) {

    override fun backupProcess(process: ProcessingProcess) {
        val terminal = process.terminal!!

        process.timeBegun = LocalDateTime.now()

        terminal.println(Markdown("""
            ## Preface
            KtSync is starting to backup your files. You chose to backup the folder ${(brightWhite)(process.profile!!.sourcePath!!)} into the folder ${(brightWhite)(process.profile!!.destinationPath!!)} using the ${(brightWhite)("Full Backup algorithm")}. If the destination already exists, it will be safely renamed, do not worry about that.
            ## Progress
        """.trimIndent()))

        propagate({
            subprocessor.backupProcess(process)
        }, {
            process.timeEnded = LocalDateTime.now()
            val elapsed = Duration.between(process.timeBegun!!, process.timeEnded!!)

            terminal.println(Markdown("""
                ## Summary
                ${(brightGreen)("All done. Everything, ${(brightWhite)("${process.successfulEntries} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))}, was backed up.")}
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can dispose of it at your discretion.    
                """.trimIndent()))
            terminal.println(Markdown("""
                
                Entire process took ${(brightWhite)(timeToHMS(elapsed))}.
            """.trimIndent()))
        }, {
            terminal.println(Markdown("""
                ## Issues
            """.trimIndent()))
            for ((path,reason) in process.failedEntries) {
                val relativePath = subprocessor.relative(path, subprocessor.absolute(process.profile!!.sourcePath!!))
                terminal.println(Markdown("""
                    * ${(brightWhite)(relativePath)} was not backed up.
                """.trimIndent()))
            }
            terminal.println(Markdown("""
                ## Summary
                ${(brightYellow)("Backup has partially failed. ${(brightWhite)("${process.successfulEntries} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))} were successfully backed up, however ${(brightWhite)("${process.failedEntriesCount} files/folders")} were not.")}
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can recover it at your discretion.    
                """.trimIndent()))
            throw it
        }, {
            terminal.println(Markdown("""
                ## Summary
                ${(brightRed)("Backup has entirely failed due to $it. Destination folder is in indeterminate state.")}
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can recover it at your discretion.    
                """.trimIndent()))
            throw it
        }, {
            terminal.println(Markdown("""
                ## Summary
                ${(brightRed)("Backup has entirely failed due to unknown exception $it. Destination folder is in indeterminate state.")}
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can recover it at your discretion.    
                """.trimIndent()))
            throw it
        })
    }

}