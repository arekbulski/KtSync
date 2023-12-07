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

        if (subprocessor.isSymbolicLink(sourcePath))
            throw FailedException("Source folder $sourcePath is a symbolic link.")
        if (subprocessor.isSymbolicLink(destinationPath))
            throw FailedException("Destination folder $destinationPath is a symbolic link.")

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
            throw FailedException("Source folder $sourcePath does not exist.", this)
        if (! subprocessor.isDirectory(sourcePath))
            throw FailedException("Source folder $sourcePath is not a folder.", this)
        folder.isFolder = true
        if (subprocessor.exists(destinationPath)) {
            if (folder.isRoot == true) {
                val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                val destinationRenamed = "${destinationPath}-trash-$datetime"
                if (! subprocessor.renameTo(destinationPath, destinationRenamed))
                    throw FailedException("Destination folder $destinationPath could not be renamed.", this)
                folder.process!!.destinationRenamedTo = subprocessor.extractName(destinationRenamed)
            }
            if (folder.isRoot == false) {
                throw FailedException("Destination folder $destinationPath already exists.", this)
            }
            // Under no scenario can this happen.
            if (folder.isRoot == null) {
                throw IllegalStateException("isRoot should not be null.")
            }
        }

        subprocessor.backupFolder(folder)

        propagate({
            if (! subprocessor.createFolder(destinationPath))
                throw FailedException("Destination folder $destinationPath failed to create.", this)
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

        val entries = subprocessor.listFolderEntries(sourcePath)
        var partiallyFailed = 0

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
                    (folder.process!!).successfulEntries++
                } else {
                    this.backupFile(subprocessing)
                    (folder.process!!).successfulEntries++
                    (folder.process!!).successfulBytes += subprocessing.size!!
                }
            } catch (e: PartiallyFailedException) {
                partiallyFailed++
                (folder.process!!).failedEntriesCount++
                (folder.process!!).failedEntries[entry] = e.description!!
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
                throw FailedException("Source file $sourcePath does not exist.", this)
            if (subprocessor.isSymbolicLink(sourcePath))
                throw FailedException("Source file $sourcePath is a symbolic link.", this)
            if (! subprocessor.isRegularFile(sourcePath))
                throw FailedException("Source file $sourcePath is not a regular file.", this)
            file.isRegularFile = true
            if (subprocessor.exists(destinationPath))
                throw FailedException("Destination file $destinationPath already exists.", this)

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