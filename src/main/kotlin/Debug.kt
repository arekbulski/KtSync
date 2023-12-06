
class Debug (
    val subprocessor: Processor
) : Processor () {

    override fun backupProcess(operation: ProcessingOperation): Result {
        operation.terminal?.println("(debug) into backupProcess (operation has no details yet)")
        val subresult = subprocessor.backupProcess(operation)
        if (subprocessor !is DoNothing)
            operation.terminal?.println("(debug) out of backupProcess (operation has no details yet)")
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