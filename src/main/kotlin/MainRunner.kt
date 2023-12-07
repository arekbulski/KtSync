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

        try {
            subprocessor.backupProcess(process)
            exitProcess(0)
        } catch (e: FailedException) {
            exitProcess(1)
        } catch (e: PartiallyFailedException) {
            exitProcess(1)
        } catch (e: Exception) {
            exitProcess(1)
        }
    }

}