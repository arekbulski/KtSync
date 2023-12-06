
class Debug (
    val subprocessor: Processor
) : Processor () {

    override fun backupProcess(process: ProcessingProcess): Result {
        process.terminal?.println("(debug) into backupProcess (process has no details yet)")
        val subresult = subprocessor.backupProcess(process)
        if (subprocessor !is DoNothing)
            // TODO: Debug methods are lacking in printing details.
            process.terminal?.println("(debug) out of backupProcess (process has no details yet)")
        return subresult
    }

    override fun backupFolder(folder: ProcessingFile): Result {
        // TODO: Debug methods are lacking in printing details.
        return subprocessor.backupFolder(folder)
    }

    override fun backupFile(file: ProcessingFile): Result {
        // TODO: Debug methods are lacking in printing details.
        return subprocessor.backupFile(file)
    }

}