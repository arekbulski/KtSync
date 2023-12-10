
// This class estimates how many files and how much data needs to be processed at most.
class PreEstimationAlgorithm (subprocessor: Processor) : Passthrough(subprocessor) {

    // This method traverses the source branch (non-recursively) and counts number and size of files and adds those up. It also displays progress while it is doing it.
    // TODO: If regular files can be top-level backed up objects, then this needs a redo.
    override fun estimateFolder(process: ProcessingProcess, folder: String) {
        process.estimatedCount = 0
        process.estimatedBytes = 0
        val queue = arrayListOf(subprocessor.absolute(folder))
        subprocessor.initEstimationProgress(process)

        while (queue.isNotEmpty()) {
            val entry = queue.removeAt(0)
            process.estimatedCount++
            try {
                if (subprocessor.isFolder(entry)) {
                    queue.addAll(subprocessor.listFolderEntries(entry))
                }
                if (subprocessor.isRegularFile(entry)) {
                    process.estimatedBytes += subprocessor.getFileSize(entry)
                }
                // Symbolic links are already counted +1 towards estimatedCount.
            }
            catch (e: Exception) {
            }
            subprocessor.updateEstimationProgress(process)
        }

        // The branch root (source folder itself) should not be counted towards it.
        process.estimatedCount--
        subprocessor.finishEstimationProgress(process)
    }

}