
class DoNothing : Processor() {

    override fun backupProcess(process: ProcessingProcess) {
        // TODO: Experimenting with Failure status and exceptions.
//        throw IllegalStateException("Something went wrong in DoNothing...")
    }

    override fun backupFolder(folder: ProcessingFile) {
    }

    override fun backupFile(file: ProcessingFile) {
    }

}