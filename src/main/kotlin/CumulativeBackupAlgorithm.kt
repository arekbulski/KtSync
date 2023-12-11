
class CumulativeBackupAlgorithm (subprocessor: Processor) : FullBackupAlgorithm(subprocessor) {

    override fun chooseCloning(file: ProcessingFile): Boolean {
        val sourcePath = file.sourcePath!!
        val previousPath = file.previousPath!!

        // TODO: Check file permissions as well?
        return subprocessor.exists(previousPath) &&
            subprocessor.getFileSize(sourcePath) == subprocessor.getFileSize(previousPath) &&
            subprocessor.getModificationTime(sourcePath) == subprocessor.getModificationTime(previousPath)
    }

}