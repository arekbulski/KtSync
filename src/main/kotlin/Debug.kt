
class Debug (
    val subprocessor: Processor
) : Processor () {

    override fun backupProcess(operation: ProcessingOperation): Result {
        // TODO: Debug methods are lacking in printing details.
        return subprocessor.backupProcess(operation)
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