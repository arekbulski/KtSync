
class DoNothing : Processor() {

    override fun backupProcess(process: ProcessingProcess): Result {
        // TODO: Experimenting with Failure status and exceptions.
//        return Result(ResultStatus.Failure, "Do you see me?", processor=this)
//        throw IllegalStateException("Something went wrong in DoNothing...")

        return Result(ResultStatus.Success, "Bouncing back from DoNothing.")
    }

    override fun backupFolder(folder: ProcessingFile): Result {
        return Result(ResultStatus.Success, "Bouncing back from DoNothing.")
    }

    override fun backupFile(file: ProcessingFile): Result {
        return Result(ResultStatus.Success, "Bouncing back from DoNothing.")
    }

}