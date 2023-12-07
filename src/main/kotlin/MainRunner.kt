import com.github.ajalt.mordant.terminal.Terminal
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
        }

        try {
            subprocessor.backupProcess(process)
            exitProcess(0)
        } catch (e: Exception) {
            exitProcess(1)
        }
    }

}