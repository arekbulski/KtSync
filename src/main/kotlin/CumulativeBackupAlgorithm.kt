
class CumulativeBackupAlgorithm (subprocessor: Processor) : FullBackupAlgorithm(subprocessor) {

    override fun chooseCloning(file: ProcessingFile): Boolean {
        val sourcePath = file.sourcePath!!
        val previousPath = file.previousPath!!

        return subprocessor.existsRemote(previousPath) &&
            subprocessor.getFileSizeLocal(sourcePath) == subprocessor.getFileSizeRemote(previousPath) &&
            subprocessor.getModificationTimeLocal(sourcePath) == subprocessor.getModificationTimeRemote(previousPath)
    }

}