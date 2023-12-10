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
                subprocessor.renameTo(destinationPath, destinationRenamed)
                process.destinationRenamedTo = subprocessor.extractName(destinationRenamed)
            } else {
                throw TotalFailureException("Destination folder $destinationPath already exists.", this)
            }
        }

        propagateCombined({
            subprocessor.initFolderProgress(folder)

            subprocessor.createFolder(destinationPath)
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
                    this.isSymbolicLink = subprocessor.isSymbolicLink(entry)
                    this.isFolder = subprocessor.isFolder(entry)
                    if (this.isRegularFile)
                        this.size = subprocessor.getSize(entry)
                }
                if (subprocessing.isFolder) {
                    // The sub-folders get copied recursively. Note that isRoot is false from now on.
                    this.backupFolder(subprocessing)
                } else if (subprocessing.isSymbolicLink) {
                    this.backupSymbolicLink(subprocessing)
                } else {
                    this.backupFile(subprocessing)
                }
            }
            catch (e: Exception) {
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
            throw PartialFailureException("Could not get/set mtime from folder $sourcePath to folder $destinationPath.", this, it)
        })

        propagateCombined({
            val permissions = subprocessor.getPosixPermissions(sourcePath)
            subprocessor.setPosixPermissions(destinationPath, permissions)
        }, null, {
            throw PartialFailureException("Could not get/set permissions from file $sourcePath to file $destinationPath.", this, it)
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

    override fun backupSymbolicLink(symlink: ProcessingFile) {
        val process = symlink.process!!
        val sourcePath = symlink.sourcePath!!
        val destinationPath = symlink.destinationPath!!

        propagateCombined({
            subprocessor.initSymbolicLinkProgress(symlink)

            // TODO: In the future, regular files will be allowed to be top-level backup objects.
            if (symlink.isRoot)
                throw TotalFailureException("A non-directory cannot be a top-level backup object.")
            if (! subprocessor.exists(sourcePath))
                throw TotalFailureException("Source symlink $sourcePath does not exist.", this)
            if (! subprocessor.isSymbolicLink(sourcePath))
                throw TotalFailureException("Source symlink $sourcePath is not a symbolic link.", this)
            symlink.isSymbolicLink = true
            if (subprocessor.exists(destinationPath))
                throw TotalFailureException("Destination symlink $destinationPath already exists.", this)

            subprocessor.copySymbolicLink(sourcePath, destinationPath)
        },{
            process.processedCount++
            process.successfulCount++
            subprocessor.finishSymbolicLinkProgress(symlink, null)
        }, {
            process.processedCount++
            subprocessor.finishSymbolicLinkProgress(symlink, it)
            throw it
        })
    }

}