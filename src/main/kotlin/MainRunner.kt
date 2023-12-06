import com.github.ajalt.mordant.terminal.Terminal

class MainRunner (
    val subprocessor: Processor
) {

    fun main () {
        var operation = ProcessingOperation().apply {
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