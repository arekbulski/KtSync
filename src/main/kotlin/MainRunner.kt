import java.io.File
import kotlin.system.exitProcess

// This class populates a ProcessingJob/JobDescription instances with details of what the operation is, runs the job, and catches any exceptions and turns them into 0/1 exit status.
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

    fun readAtedString (text:String): String {
        if (text.startsWith("@"))
            return File(text.removePrefix("@")).readText(Charsets.UTF_8)
        return text
    }

}