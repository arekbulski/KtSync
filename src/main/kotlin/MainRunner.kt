import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import kotlin.system.exitProcess

class MainRunner (
    val subprocessor: Processor
) {

    fun main () {
        // TODO: This configuration needs to be read from a JSON file or from the terminal.
        val process = ProcessingProcess().apply {
            profile = Profile().apply {
                operation = "backup"
                algorithm = "full"
                sourcePath = "temporary/source"
                destinationPath = "temporary/destination1"
            }
            terminal = Terminal()
            successfulEntries = 0
            successfulBytes = 0
            failedEntries = 0
        }
        val terminal = process.terminal!!

        try {
            subprocessor.backupProcess(process)
            terminal.println((brightGreen)("Done."))
            exitProcess(0)
        } catch (e: FailedException) {
            terminal.println((brightRed)("Aborting."))
            exitProcess(1)
        } catch (e: PartiallyFailedException) {
            terminal.println((brightYellow)("Aborting."))
            exitProcess(1)
        }
    }

}