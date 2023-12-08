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

        return this.backupFolder(root)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun backupFolder(folder: ProcessingFile) {
        val sourcePath = folder.sourcePath!!
        val destinationPath = folder.destinationPath!!

        if (! subprocessor.exists(sourcePath))
            throw TotallyFailedException("Source folder $sourcePath does not exist.", this)
        if (! subprocessor.isDirectory(sourcePath))
            throw TotallyFailedException("Source folder $sourcePath is not a folder.", this)
        folder.isFolder = true
        if (subprocessor.exists(destinationPath)) {
            if (folder.isRoot == true) {
                val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                val destinationRenamed = "${destinationPath}-trash-$datetime"
                if (! subprocessor.renameTo(destinationPath, destinationRenamed))
                    throw TotallyFailedException("Destination folder $destinationPath could not be renamed.", this)
                folder.process!!.destinationRenamedTo = subprocessor.extractName(destinationRenamed)
            }
            if (folder.isRoot == false) {
                throw TotallyFailedException("Destination folder $destinationPath already exists.", this)
            }
            // Under no scenario can this happen.
            if (folder.isRoot == null) {
                throw IllegalStateException("isRoot should not be null.")
            }
        }

        subprocessor.backupFolder(folder)

        propagate({
            if (! subprocessor.createFolder(destinationPath))
                throw TotallyFailedException("Destination folder $destinationPath failed to create.", this)
        },{
            subprocessor.finishFolder(folder, true, null)
        }, {
            // Under no scenario can this happen.
            subprocessor.finishFolder(folder, null, it.description)
            throw it
        }, {
            subprocessor.finishFolder(folder, false, it.description)
            throw it
        }, {
            subprocessor.finishFolder(folder, false, it.toString())
            throw it
        })

        val process = folder.process!!
        val entries = subprocessor.listFolderEntries(sourcePath)
        var partiallyFailed = 0L

        for (entry in entries) {
            try {
                val subprocessing = ProcessingFile().apply {
                    this.process = folder.process
                    this.sourcePath = entry
                    this.destinationPath = subprocessor.resolve(destinationPath, subprocessor.extractName(entry))
                    this.isRoot = false
                    this.isRegularFile = subprocessor.isRegularFile(entry)
                    this.isFolder = subprocessor.isDirectory(entry)
                    if (this.isRegularFile == true)
                        this.size = subprocessor.getSize(entry)
                }
                if (subprocessing.isFolder == true) {
                    this.backupFolder(subprocessing)
                    process.successfulEntries++
                } else {
                    this.backupFile(subprocessing)
                    process.successfulEntries++
                    process.successfulBytes += subprocessing.size!!
                }
            } catch (e: PartiallyFailedException) {
                partiallyFailed++
                process.failedEntriesCount++
                process.failedEntries[entry] = e.description!!
            }
        }

        if (partiallyFailed > 0)
            throw PartiallyFailedException("Source folder $sourcePath failed to backup $partiallyFailed entries.", this)
    }

    @ExperimentalUnsignedTypes
    override fun backupFile(file: ProcessingFile) {
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
            subprocessor.finishFile(file, true, null)
        }, {
            subprocessor.finishFile(file, null, it.description)
            throw it
        }, {
            subprocessor.finishFile(file, false, it.description)
            throw it
        }, {
            subprocessor.finishFile(file, false, it.toString())
            throw it
        })
    }

}