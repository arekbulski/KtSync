import com.github.ajalt.mordant.markdown.Markdown

class PrettyPrintAlgorithm (
    val subprocessor: Processor
) : Processor() {

    override fun backupProcess(operation: ProcessingOperation): Result {
        val terminal = operation.terminal!!

        terminal.println(Markdown("""
            ## Debug
            Beginning a test run (hard-coded operation).
        """.trimIndent()))

        // TODO: Check result for failure maybe?
        subprocessor.backupProcess(operation)

        terminal.println(Markdown("""
            All done.
        """.trimIndent()))

        return Result(ResultStatus.Success)
    }

    override fun backupFolder(folder: ProcessingFile): Result {
        TODO("Not yet implemented")
    }

    override fun backupFile(file: ProcessingFile): Result {
        TODO("Not yet implemented")
    }

}