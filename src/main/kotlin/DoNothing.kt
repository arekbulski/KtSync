
class DoNothing : Processor() {

    override fun backupProcess(operation: ProcessingOperation): Result {
        return Result(ResultStatus.Success, "Bouncing back from DoNothing.")
    }

    override fun backupFolder(folder: ProcessingFile): Result {
        return Result(ResultStatus.Success, "Bouncing back from DoNothing.")
    }

    override fun backupFile(file: ProcessingFile): Result {
        return Result(ResultStatus.Success, "Bouncing back from DoNothing.")
    }

}