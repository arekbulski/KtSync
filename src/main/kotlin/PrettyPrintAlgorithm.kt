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
            val throughput = process.successfulBytes.toDouble() / (elapsed.toMillis().toDouble() / 1000.0)

            terminal.println(Markdown("""
                ## Summary
                ${(brightGreen)("Backup was successful. ${(brightWhite)("${process.successfulCount} files/folders")} totaling ${(brightWhite)(suffixedSize(process.successfulBytes))} were backed up.")}
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can dispose of it at your discretion.    
                """.trimIndent()))
            terminal.println(Markdown("""
                
                The average throughput was ${(brightWhite)(suffixedThroughput(throughput))} as sending ${(brightWhite)(suffixedSize(process.successfulBytes))} took you ${(brightWhite)(timeToHMS(elapsed))} time. Note that uplink speed is usually the minimum between source backend and destination backend, and rarely compression or encryption layer.
            """.trimIndent()))

        }, {
            terminal.println(Markdown("""
                ## Issues
            """.trimIndent()))
            for ((path, exception) in process.failedEntries) {
                val relativePath = subprocessor.relative(path, subprocessor.absolute(process.profile!!.sourcePath!!))
                terminal.println(Markdown("""
                    * ${(brightWhite)(relativePath)} was not backed up due to ${(if (exception is TotallyFailedException) brightRed else brightYellow)(exception.toString())}.
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
                Reason: $it
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
                Reason: $it
            """.trimIndent()))
            if (process.destinationRenamedTo != null)
                terminal.println(Markdown("""
                    
                    Your previous backup was renamed to ${(brightWhite)(process.destinationRenamedTo!!)}. You can recover it at your discretion.    
                """.trimIndent()))
            throw it
        })
    }

}