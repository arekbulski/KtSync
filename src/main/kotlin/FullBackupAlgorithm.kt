import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FullBackupAlgorithm (
    val subprocessor: Processor
) : Processor() {

    override fun backupProcess(process: ProcessingProcess) {
        val profile = process.profile!!
        val sourcePath = profile.sourcePath!!
        val destinationPath = profile.destinationPath!!

        subprocessor.backupProcess(process)

        if (subprocessor.isSymbolicLink(sourcePath))
            throw FailedException("Source folder $sourcePath is a symbolic link.")
        if (subprocessor.isSymbolicLink(destinationPath))
            throw FailedException("Destination folder $destinationPath is a symbolic link.")

        val root = ProcessingFile().apply {
            this.process = process
            this.sourcePath = subprocessor.canonical(sourcePath)
            this.destinationPath = subprocessor.canonical(destinationPath)
            this.isRoot = true
        }

        return this.backupFolder(root)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun backupFolder(folder: ProcessingFile) {
        val sourcePath = folder.sourcePath!!
        val destinationPath = folder.destinationPath!!

        if (! subprocessor.exists(sourcePath))
            throw FailedException("Source folder $sourcePath does not exist.", null, this)
        if (! subprocessor.isDirectory(sourcePath))
            throw FailedException("Source folder $sourcePath is not a folder.", null, this)
        folder.isFolder = true
        if (subprocessor.exists(destinationPath)) {
            if (folder.isRoot == true) {
                val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                val destinationRenamed = "${destinationPath}-trash-$datetime"
                if (! subprocessor.renameTo(destinationPath, destinationRenamed))
                    throw FailedException("Destination folder $destinationPath could not be renamed.", null, this)
            }
            if (folder.isRoot == false) {
                throw FailedException("Destination folder $destinationPath already exists.", null, this)
            }
            if (folder.isRoot == null) {
                throw IllegalStateException("isRoot should not be null.")
            }
        }

        if (! subprocessor.createFolder(destinationPath))
            throw FailedException("Destination folder $destinationPath failed to create.", null, this)

        subprocessor.backupFolder(folder)

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
                }
                if (subprocessing.isFolder == true) {
                    this.backupFolder(subprocessing)
                    (folder.process!!).successfulEntries++
                }
                if (subprocessing.isRegularFile == true) {
                    this.backupFile(subprocessing)
                    (folder.process!!).successfulEntries++
                    // TODO: File size to be determined.
                    (folder.process!!).successfulBytes += 1024
                }
                if ((subprocessing.isRegularFile != true) && (subprocessing.isFolder != true)) {
                    throw FailedException("Source entry $entry is not a regular file nor a folder.", null, this)
                }
            } catch (e: PartiallyFailedException) {
                partiallyFailed++
                (folder.process!!).failedEntries++
                // TODO: File size to be determined.
                (folder.process!!).failedBytes += 1024
            }
        }

        // TODO: How do I pretty print this?
        if (partiallyFailed > 0)
            throw PartiallyFailedException("Source folder $sourcePath failed to backup $partiallyFailed entries.", null, this)
    }

    @ExperimentalUnsignedTypes
    override fun backupFile(file: ProcessingFile) {
        val sourcePath = file.sourcePath!!
        val destinationPath = file.destinationPath!!

        if (file.isRoot == true)
            throw IllegalStateException("isRoot should be false or null.")
        if (! subprocessor.exists(sourcePath))
            throw FailedException("Source file $sourcePath does not exist.", null, this)
        if (! subprocessor.isRegularFile(sourcePath))
            throw FailedException("Source file $sourcePath is not a regular file.", null, this)
        file.isRegularFile = true
        if (subprocessor.exists(destinationPath))
            throw FailedException("Destination file $destinationPath already exists.", null, this)

        subprocessor.backupFile(file)

//        if (! subprocessor.createRegularFile(destinationPath))
//            throw FailedException("Destination file $destinationPath failed to create.", null, this)

        val data = subprocessor.readFileContent(sourcePath)
        subprocessor.writeFileContent(destinationPath, data)
    }

}