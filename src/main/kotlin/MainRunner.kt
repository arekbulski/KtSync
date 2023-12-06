import com.github.ajalt.mordant.terminal.Terminal

class MainRunner (
    val subprocessor: Processor
) {

    fun main () {
        // TODO: This configuration needs to be read from a JSON file or from the terminal.
        var operation = ProcessingProcess().apply {
            profile = Profile().apply {
                operation = "backup"
                algorithm = "full"
                sourcePath = "temporary/source"
                destinationPath = "temporary/destination1"
            }
            terminal = Terminal()
        }

        subprocessor.backupProcess(operation)
    }

}