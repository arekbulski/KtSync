import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import com.github.ajalt.mordant.terminal.Terminal
import java.time.Duration
import java.time.LocalDateTime

// This class establishes pretty printed chapters and results. Note that this class does not handle pretty printing of individual files and folders, or while streaming files. That is the job of PrettyPrintFiles class.
class PrettyPrintAlgorithm (subprocessor: Processor) : Passthrough(subprocessor) {

    override fun backupProcess(process: ProcessingProcess) {
        val profile = process.profile!!

        process.terminal = Terminal()
        val terminal = process.terminal!!

        terminal.println(Markdown("""
            ## Preface
            KtSync is starting to backup your files. You chose to backup the folder ${(brightWhite)(profile.sourcePath!!)} into the folder ${(brightWhite)(profile.destinationPath!!)} using the ${(brightWhite)("Full Backup algorithm")}. If the destination already exists, it will be safely renamed, do not worry about that.
            ## Progress
        """.trimIndent()))

        propagateArms({
            process.processingBegun = LocalDateTime.now()
            subprocessor.backupProcess(process)
        }, {
            process.processingEnded = LocalDateTime.now()
            val elapsed = Duration.between(process.processingBegun!!, process.processingEnded!!)
            val throughputInBytes = process.successfulBytes / (elapsed.toMillis().toDouble() / 1000.0)
            val throughputInFiles = (process.successfulCount / (elapsed.toMillis().toDouble() / 1000.0))

            terminal.println(Markdown("""
                ## Summary
                ${(brightGreen)("Backup was successful. ${(brightWhite)("${process.successfulCount} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))} were backed up.")}
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can dispose of it at your discretion.    
                """.trimIndent()))
            terminal.println(Markdown("""
                
                The average throughput was ${(brightWhite)(suffixedByteThroughput(throughputInBytes))} (or ${(brightWhite)(suffixedFileThroughput(throughputInFiles))}) as sending ${(brightWhite)(suffixedSize(process.successfulBytes))} (or ${(brightWhite)(suffixedCount(process.successfulCount))}) took you ${(brightWhite)(timeToHMSM(elapsed))} time.
            """.trimIndent()))

        }, {
            terminal.println(Markdown("""
                ## Issues
            """.trimIndent()))
            for ((path, exception) in process.failedEntries) {
                val relativePath = subprocessor.relative(path, subprocessor.absolute(process.profile!!.sourcePath!!))
                // TODO: Fix the red/yellow coloring.
                terminal.println(Markdown("""
                    * ${(brightWhite)(relativePath)} was not backed up due to ${(if (exception is TotalFailureException) brightRed else brightYellow)(exception.toString())}.
                """.trimIndent()))
            }
            terminal.println(Markdown("""
                ## Summary
                ${(brightYellow)("Backup was partially successful. ${(brightWhite)("${process.successfulCount} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))} were successfully backed up, however ${(brightWhite)("${process.failedEntries.size} files/folders")} were not.")}
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can recover it at your discretion.    
                """.trimIndent()))
            throw it

        }, {
            terminal.println(Markdown("""
                ## Summary
                ${(brightRed)("Backup has failed. Destination folder is in indeterminate state.")}
                $it
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can recover it at your discretion.    
                """.trimIndent()))
            throw it

        }, {
            terminal.println(Markdown("""
                ## Summary
                ${(brightRed)("Backup has failed. Destination folder is in indeterminate state.")}
                $it
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can recover it at your discretion.    
                """.trimIndent()))
            throw it
        })
    }

}