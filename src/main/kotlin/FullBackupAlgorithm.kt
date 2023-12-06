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

        if (Files.isSymbolicLink(File(sourcePath).toPath()))
            throw FailedException("Source folder $sourcePath is a symbolic link.")
        if (Files.isSymbolicLink(File(destinationPath).toPath()))
            throw FailedException("Source folder $destinationPath is a symbolic link.")

        val sourceBranch = File(sourcePath).canonicalFile
        val destinationBranch = File(destinationPath).canonicalFile

        val root = ProcessingFile().apply {
            this.process = process
            sourcePathname = sourceBranch.canonicalPath
            destinationPathname = destinationBranch.canonicalPath
            isRoot = true
        }

        return this.backupFolder(root)
    }

    override fun backupFolder(folder: ProcessingFile) {
        val sourceBranch = File(folder.sourcePathname!!)
        val destinationBranch = File(folder.destinationPathname!!)

        if (! Files.exists(sourceBranch.toPath(), LinkOption.NOFOLLOW_LINKS))
            throw FailedException("Source folder $sourceBranch does not exist.", null, this)
        if (! Files.isDirectory(sourceBranch.toPath(), LinkOption.NOFOLLOW_LINKS))
            throw FailedException("Source folder $sourceBranch is not a folder.", null, this)
        folder.isFolder = true
        if (Files.exists(destinationBranch.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            if (folder.isRoot == true) {
                val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                val destinationRenamed = File("${destinationBranch.canonicalPath}-trash-$datetime")
                if (! destinationBranch.renameTo(destinationRenamed))
                    throw FailedException("Destination folder $destinationBranch could not be renamed.", null, this)
            }
            if (folder.isRoot == false) {
                throw FailedException("Destination folder $destinationBranch already exists.", null, this)
            }
            if (folder.isRoot == null) {
                throw IllegalStateException("isRoot should not be null.")
            }
        }

        if (! destinationBranch.mkdir())
            throw FailedException("Destination folder $destinationBranch failed to create.", null, this)

        subprocessor.backupFolder(folder)

        val subfiles = sourceBranch.listFiles()
            ?: throw FailedException("Source folder $sourceBranch failed to list entries.", null, this)
        var partiallyFailed = 0
        for (subfile in subfiles) {
            try {
                val subprocessing = ProcessingFile().apply {
                    process = folder.process
                    sourcePathname = subfile.absolutePath
                    destinationPathname = destinationBranch.resolve(subfile.name).canonicalPath
                    isRoot = false
                    isRegularFile = Files.isRegularFile(subfile.toPath(), LinkOption.NOFOLLOW_LINKS)
                    isFolder = Files.isDirectory(subfile.toPath(), LinkOption.NOFOLLOW_LINKS)
                }
                if (subprocessing.isFolder == true) {
                    this.backupFolder(subprocessing)
                }
                if (subprocessing.isRegularFile == true) {
                    this.backupFile(subprocessing)
                }
                if ((subprocessing.isRegularFile != true) && (subprocessing.isFolder != true)) {
                    throw FailedException("Source entry $subfile is not a regular file nor a folder.", null, this)
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
            throw PartiallyFailedException("Source folder $sourceBranch failed to backup $partiallyFailed entries.", null, this)
    }

    override fun backupFile(file: ProcessingFile) {
        val sourceNode = File(file.sourcePathname!!)
        val destinationNode = File(file.destinationPathname!!)

        if (file.isRoot == true)
            throw IllegalStateException("isRoot should be false or null.")
        if (! Files.exists(sourceNode.toPath(), LinkOption.NOFOLLOW_LINKS))
            throw FailedException("Source file $sourceNode does not exist.", null, this)
        if (! Files.isRegularFile(sourceNode.toPath(), LinkOption.NOFOLLOW_LINKS))
            throw FailedException("Source file $sourceNode is not a regular file.", null, this)
        file.isRegularFile = true
        if (Files.exists(destinationNode.toPath(), LinkOption.NOFOLLOW_LINKS))
                throw FailedException("Destination file $destinationNode already exists.", null, this)

        if (! destinationNode.createNewFile())
            throw FailedException("Destination file $destinationNode failed to create.", null, this)

        subprocessor.backupFile(file)
    }

}