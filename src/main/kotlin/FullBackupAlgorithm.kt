import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// This class handles the meat, meaning it traverses the source folder, establishes file-to-file correspondence, copies everything recursively. It also calls processor below to pretty print progress bars and statuses.
class FullBackupAlgorithm (subprocessor: Processor) : Passthrough(subprocessor) {

    override fun backupProcess(process: ProcessingProcess) {
        val profile = process.profile!!
        val sourcePath = profile.sourcePath!!
        val destinationPath = profile.destinationPath!!

        // TODO: Perhaps top-level folder should be included into total count.
        this.estimateFolder(process, sourcePath)

        val root = ProcessingFile().apply {
            this.process = process
            this.sourcePath = subprocessor.absolute(sourcePath)
            this.destinationPath = subprocessor.absolute(destinationPath)
            this.isRoot = true
        }
        // The top-level folder gets copied recursively. Note that isRoot is true.
        this.backupFolder(root)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun backupFolder(folder: ProcessingFile) {
        val process = folder.process!!
        val sourcePath = folder.sourcePath!!
        val destinationPath = folder.destinationPath!!

        if (! subprocessor.exists(sourcePath))
            throw TotalFailureException("Source folder $sourcePath does not exist.", this)
        if (! subprocessor.isFolder(sourcePath))
            throw TotalFailureException("Source folder $sourcePath is not a folder.", this)
        folder.isFolder = true
        if (subprocessor.exists(destinationPath)) {
            if (folder.isRoot) {
                val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                val destinationRenamed = "${destinationPath}-trash-$datetime"
                if (! subprocessor.renameTo(destinationPath, destinationRenamed))
                    throw TotalFailureException("Destination folder $destinationPath could not be renamed.", this)
                process.destinationRenamedTo = subprocessor.extractName(destinationRenamed)
            } else {
                throw TotalFailureException("Destination folder $destinationPath already exists.", this)
            }
        }

        subprocessor.initFolderProgress(folder)

        propagate({
            if (! subprocessor.createFolder(destinationPath))
                throw TotalFailureException("Destination folder $destinationPath failed to create.", this)
        },{
            if (! folder.isRoot)
              process.processedCount++
            subprocessor.finishFolderProgress(folder, true, null)
        }, {
            if (! folder.isRoot)
                process.processedCount++
            subprocessor.finishFolderProgress(folder, null, it.toString())
            throw it
        }, {
            if (! folder.isRoot)
                process.processedCount++
            subprocessor.finishFolderProgress(folder, false, it.toString())
            throw it
        }, {
            if (! folder.isRoot)
                process.processedCount++
            subprocessor.finishFolderProgress(folder, false, it.toString())
            throw it
        })

        val entries = subprocessor.listFolderEntries(sourcePath)
        var failedLocally = 0L

        for (entry in entries) {
            try {
                val subprocessing = ProcessingFile().apply {
                    this.process = folder.process
                    this.sourcePath = entry
                    this.destinationPath = subprocessor.resolve(destinationPath, subprocessor.extractName(entry))
                    this.isRoot = false
                    this.isRegularFile = subprocessor.isRegularFile(entry)
                    this.isFolder = subprocessor.isFolder(entry)
                    if (this.isRegularFile)
                        this.size = subprocessor.getSize(entry)
                }
                if (subprocessing.isFolder) {
                    // The sub-folders get copied recursively. Note that isRoot is false from now on.
                    this.backupFolder(subprocessing)
                } else {
                    // TODO: This method is called for everything but folders (ie. symbolic links).
                    this.backupFile(subprocessing)
                }
            }
            // This catches both TotalFailure and PartialFailure, but not general exceptions.
            catch (e: PartialFailureException) {
                failedLocally++
                process.failedEntries[entry] = e
            }
        }

        if (failedLocally > 0)
            throw PartialFailureException("Source folder $sourcePath failed to backup $failedLocally entries.", this)

        if (! folder.isRoot)
            process.successfulCount++
    }

    @ExperimentalUnsignedTypes
    override fun backupFile(file: ProcessingFile) {
        val process = file.process!!
        val sourcePath = file.sourcePath!!
        val destinationPath = file.destinationPath!!

        subprocessor.initFileProgress(file)

        propagate({
            // TODO: In the future, regular files will be allowed to be top-level backup objects.
            if (file.isRoot)
                throw TotalFailureException("A non-directory cannot be a top-level backup object.")
            if (! subprocessor.exists(sourcePath))
                throw TotalFailureException("Source file $sourcePath does not exist.", this)
            // TODO: Allow backing up symbolic links.
            if (subprocessor.isSymbolicLink(sourcePath))
                throw TotalFailureException("Source file $sourcePath is a symbolic link.", this)
            if (! subprocessor.isRegularFile(sourcePath))
                throw TotalFailureException("Source file $sourcePath is not a regular file.", this)
            file.isRegularFile = true
            if (subprocessor.exists(destinationPath))
                throw TotalFailureException("Destination file $destinationPath already exists.", this)

            // TODO: Change file read-all/write-all to streaming.
            val data = subprocessor.readFileContent(sourcePath)
            subprocessor.writeFileContent(destinationPath, data)
        },{
            process.processedCount++
            process.processedBytes += file.size
            process.successfulCount++
            process.successfulBytes += file.size
            subprocessor.finishFileProgress(file, true, null)
        }, {
            process.processedCount++
            process.processedBytes += file.size
            subprocessor.finishFileProgress(file, null, it.description)
            throw it
        }, {
            process.processedCount++
            process.processedBytes += file.size
            subprocessor.finishFileProgress(file, false, it.description)
            throw it
        }, {
            process.processedCount++
            process.processedBytes += file.size
            subprocessor.finishFileProgress(file, false, it.toString())
            throw it
        })
    }

    // This method traverses the source tree (non-recursively) and counts number and size of files and adds those up. It also displays progress while it is doing it.
    override fun estimateFolder(process: ProcessingProcess, folder: String) {
        process.estimatedCount = 0
        process.estimatedBytes = 0
        val queue = arrayListOf(subprocessor.absolute(folder))
        subprocessor.initEstimationProgress(process)

        while (queue.isNotEmpty()) {
            val entry = queue.removeAt(0)
            if (subprocessor.isFolder(entry)) {
                process.estimatedCount++
                queue.addAll(subprocessor.listFolderEntries(entry))
            }
            if (subprocessor.isRegularFile(entry)) {
                process.estimatedCount++
                process.estimatedBytes += subprocessor.getSize(entry)
            }
            subprocessor.updateEstimationProgress(process)
        }

        process.estimatedCount--
        subprocessor.finishEstimationProgress(process)
    }

}