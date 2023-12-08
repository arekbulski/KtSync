import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FullBackupAlgorithm (
    subprocessor: Processor
) : Passthrough(subprocessor) {

    override fun backupProcess(process: ProcessingProcess) {
        val profile = process.profile!!
        val sourcePath = profile.sourcePath!!
        val destinationPath = profile.destinationPath!!

        // Currently this does nothing.
        subprocessor.backupProcess(process)

        val estimated = this.estimateFolder(sourcePath)
        process.estimatedCount = estimated.totalCount
        process.estimatedBytes = estimated.totalBytes

        // TODO: Change semantics. Allow top-level folders to be symbolic links. Just print a warning?
        if (subprocessor.isSymbolicLink(sourcePath))
            throw TotallyFailedException("Source folder $sourcePath is a symbolic link.")
        if (subprocessor.isSymbolicLink(destinationPath))
            throw TotallyFailedException("Destination folder $destinationPath is a symbolic link.")

        val root = ProcessingFile().apply {
            this.process = process
            this.sourcePath = subprocessor.absolute(sourcePath)
            this.destinationPath = subprocessor.absolute(destinationPath)
            this.isRoot = true
        }

        this.backupFolder(root)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun backupFolder(folder: ProcessingFile) {
        val process = folder.process!!
        val sourcePath = folder.sourcePath!!
        val destinationPath = folder.destinationPath!!

        if (! subprocessor.exists(sourcePath))
            throw TotallyFailedException("Source folder $sourcePath does not exist.", this)
        if (! subprocessor.isFolder(sourcePath))
            throw TotallyFailedException("Source folder $sourcePath is not a folder.", this)
        folder.isFolder = true
        if (subprocessor.exists(destinationPath)) {
            if (folder.isRoot!! == true) {
                val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                val destinationRenamed = "${destinationPath}-trash-$datetime"
                if (! subprocessor.renameTo(destinationPath, destinationRenamed))
                    throw TotallyFailedException("Destination folder $destinationPath could not be renamed.", this)
                process.destinationRenamedTo = subprocessor.extractName(destinationRenamed)
            }
            if (folder.isRoot!! == false) {
                throw TotallyFailedException("Destination folder $destinationPath already exists.", this)
            }
        }

        subprocessor.backupFolder(folder)

        propagate({
            if (! subprocessor.createFolder(destinationPath))
                throw TotallyFailedException("Destination folder $destinationPath failed to create.", this)
        },{
            if (folder.isRoot == false)
              process.processedCount++

            subprocessor.finishFolder(folder, true, null)
        }, {
            if (folder.isRoot == false)
                process.processedCount++

            // Under no scenario can this happen.
            subprocessor.finishFolder(folder, null, it.toString())
            throw it
        }, {
            if (folder.isRoot == false)
                process.processedCount++

            subprocessor.finishFolder(folder, false, it.toString())
            throw it
        }, {
            if (folder.isRoot == false)
                process.processedCount++

            subprocessor.finishFolder(folder, false, it.toString())
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
                    if (this.isRegularFile == true)
                        this.size = subprocessor.getSize(entry)
                }
                if (subprocessing.isFolder == true) {
                    this.backupFolder(subprocessing)
                } else {
                    this.backupFile(subprocessing)
                }
            } catch (e: PartiallyFailedException) {
                failedLocally++
                process.failedEntries[entry] = e
            }
        }

        if (failedLocally > 0)
            throw PartiallyFailedException("Source folder $sourcePath failed to backup $failedLocally entries.", this)

        if (folder.isRoot == false)
            process.successfulCount++
    }

    @ExperimentalUnsignedTypes
    override fun backupFile(file: ProcessingFile) {
        val process = file.process!!
        val sourcePath = file.sourcePath!!
        val destinationPath = file.destinationPath!!

        subprocessor.backupFile(file)

        propagate({
            // Under no scenario can this happen.
            if (file.isRoot == true)
                throw IllegalStateException("isRoot should be false or null.")
            if (! subprocessor.exists(sourcePath))
                throw TotallyFailedException("Source file $sourcePath does not exist.", this)
            if (subprocessor.isSymbolicLink(sourcePath))
                throw TotallyFailedException("Source file $sourcePath is a symbolic link.", this)
            if (! subprocessor.isRegularFile(sourcePath))
                throw TotallyFailedException("Source file $sourcePath is not a regular file.", this)
            file.isRegularFile = true
            if (subprocessor.exists(destinationPath))
                throw TotallyFailedException("Destination file $destinationPath already exists.", this)

            val data = subprocessor.readFileContent(sourcePath)
            subprocessor.writeFileContent(destinationPath, data)
        },{
            process.processedCount++
            process.processedBytes += file.size
            process.successfulCount++
            process.successfulBytes += file.size

            subprocessor.finishFile(file, true, null)
        }, {
            process.processedCount++
            process.processedBytes += file.size

            subprocessor.finishFile(file, null, it.description)
            throw it
        }, {
            process.processedCount++
            process.processedBytes += file.size

            subprocessor.finishFile(file, false, it.description)
            throw it
        }, {
            process.processedCount++
            process.processedBytes += file.size

            subprocessor.finishFile(file, false, it.toString())
            throw it
        })
    }

    data class EstimatedPair (val totalCount: Long, val totalBytes: Long)

    override fun estimateFolder(folder: String): EstimatedPair {
        var count = 0L
        var bytes = 0L
        val entries = subprocessor.listFolderEntries(folder)
        for (entry in entries) {
            try {
                if (subprocessor.isFolder(entry)) {
                    val sub = estimateFolder(entry)
                    count += sub.totalCount + 1
                    bytes += sub.totalBytes
                } else
                if (subprocessor.isRegularFile(entry)) {
                    count++
                    bytes += subprocessor.getSize(entry)
                } else {
                    count++
                }
            } catch (e: PartiallyFailedException) {
                count++
            }
        }
        return EstimatedPair(count, bytes)
    }

}