
// This is basically FullBackupAlgorithm but allows for cloning unmodified files. Rest is the same.
class CumulativeBackupAlgorithm (subprocessor: Processor) : FullBackupAlgorithm(subprocessor) {

    override fun chooseCloning(file: ProcessingFile): Boolean {
        val sourcePath = file.sourcePath!!
        val previousPath = file.previousPath!!

        // TODO: the 2 remote calls could be combined into 1.
        return subprocessor.existsRemote(previousPath) &&
            subprocessor.getMetadataLocal(sourcePath).isSameAs(subprocessor.getMetadataRemote(previousPath))
    }

}