import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextColors.brightRed

class PrettyPrintAlgorithm (
    val subprocessor: Processor
) : Processor() {

    override fun backupProcess(operation: ProcessingOperation): Result {
        val terminal = operation.terminal!!

        terminal.println(Markdown("""
            ## Debug
            Beginning a test run (hard-coded operation).
        """.trimIndent()))

        passthrough({
            subprocessor.backupProcess(operation)
        }, {
            terminal.println("backupProcess() came back, all green, $it")
        }, {
            terminal.println((brightRed)(it.toString()))
        })

        // TODO: Mixes green with yellow red. This needs a rework.
        terminal.println("All done.")

        return Result(ResultStatus.Success)
    }

    override fun backupFolder(folder: ProcessingFile): Result {
        throw NotImplementedError("This method was never supposed to be called.")
    }

    override fun backupFile(file: ProcessingFile): Result {
        throw NotImplementedError("This method was never supposed to be called.")
    }

}