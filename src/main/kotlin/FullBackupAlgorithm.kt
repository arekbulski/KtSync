import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// This class handles the meat, meaning it traverses the source folder, establishes file-to-file correspondence, copies everything recursively. It also calls processor below to pretty print progress bars and statuses.
class FullBackupAlgorithm (subprocessor: Processor) : Passthrough(subprocessor) {

    override fun backupProcess(process: ProcessingProcess) {
        val profile = process.profile!!
        val sourcePath = profile.sourcePath!!
        val destinationPath = profile.destinationPath!!

        subprocessor.estimateFolder(process, sourcePath)

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

        propagateCombined({
            subprocessor.initFolderProgress(folder)

            if (! subprocessor.createFolder(destinationPath))
                throw TotalFailureException("Destination folder $destinationPath failed to create.", this)
        },{
            if (! folder.isRoot)
              process.processedCount++
            subprocessor.finishFolderProgress(folder, null)
        }, {
            if (! folder.isRoot)
                process.processedCount++
            subprocessor.finishFolderProgress(folder, it)
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

        propagateCombined({
            val mtime = subprocessor.getModificationTime(sourcePath)
            subprocessor.setModificationTime(destinationPath, mtime)
        }, null, {
            throw PartialFailureException("Could not get/set mtime from folder $sourcePath to $destinationPath.", this, it)
        })


        if (! folder.isRoot)
            process.successfulCount++
    }

    @ExperimentalUnsignedTypes
    override fun backupFile(file: ProcessingFile) {
        val process = file.process!!
        val sourcePath = file.sourcePath!!
        val destinationPath = file.destinationPath!!

        propagateCombined({
            subprocessor.initFileProgress(file)

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

            val progressBefore = process.processedBytes
            val progressExpectedAfter = progressBefore + file.size
            subprocessor.copyFileProgressively(sourcePath, destinationPath,
                { at -> subprocessor.updateFileProgress(file, progressBefore + at) },
                { subprocessor.updateFileProgress(file, progressExpectedAfter) },
                { subprocessor.updateFileProgress(file, progressExpectedAfter) })

            propagateCombined({
                val mtime = subprocessor.getModificationTime(sourcePath)
                subprocessor.setModificationTime(destinationPath, mtime)
            }, null, {
                throw PartialFailureException("Could not get/set mtime from file $sourcePath to $destinationPath.", this, it)
            })
        },{
            process.processedCount++
            process.processedBytes += file.size
            process.successfulCount++
            process.successfulBytes += file.size
            subprocessor.finishFileProgress(file, null)
        }, {
            process.processedCount++
            process.processedBytes += file.size
            subprocessor.finishFileProgress(file, it)
            throw it
        })
    }

}