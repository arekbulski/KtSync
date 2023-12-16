import kotlin.system.exitProcess

// This class populates a ProcessingProcess/Profile instances with details of what the operation is, creates a terminal, and catches any exceptions and turns them into 0/1 exit status.
class MainRunner (val subprocessor: Processor) {

    fun run () {
        // TODO: This configuration needs to be read from a JSON file or from the terminal. For now the operation arguments are hardcoded into the code.
        val process = ProcessingJob().apply {
            jobDescription = JobDescription().apply {
                operation = "backup"
                algorithm = "full"
                sourcePath = "temporary/source"
                destinationPath = "temporary/destination1"
            }
        }

        try {
            subprocessor.backupProcess(process)
            exitProcess(0)
        }
        catch (e: Exception) {
            exitProcess(1)
        }
    }

}