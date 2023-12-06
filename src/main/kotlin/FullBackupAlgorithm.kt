import java.io.File

class FullBackupAlgorithm (
    val subprocessor: Processor
) : Processor() {

    override fun backupProcess(operation: ProcessingOperation): Result {
        val root = ProcessingFile().apply {
            this.operation = operation
            this.sourcePathname = File(operation.profile!!.sourcePath!!).canonicalPath
            this.destinationPathname = File(operation.profile!!.destinationPath!!).canonicalPath
            this.isFolder = true
        }
        return this.backupFolder(root)
    }

    override fun backupFolder(folder: ProcessingFile): Result {
        // TODO: Try catch entire body, rethrow a Failure?
        // TODO: Check if really a directory, not just the class field.

        val sourceBranch = File(folder.sourcePathname!!)
        val destinationBranch = File(folder.destinationPathname!!)

        if (! sourceBranch.exists())
            return Result(ResultStatus.Failure, "Source folder ${sourceBranch} does not exist.")
        if (destinationBranch.exists())
            return Result(ResultStatus.Failure, "Destination folder ${destinationBranch} already exists.")
        if (! destinationBranch.mkdir())
            return Result(ResultStatus.Failure, "Destination folder ${destinationBranch} failed to create.")

        val result = passthrough({
            subprocessor.backupFolder(folder)
        })
        // TODO: What to do?

        // TODO: listFiles could throw IOException, right?
        val subfiles = sourceBranch.listFiles()
        if (subfiles == null)
            return Result(ResultStatus.Failure, "Source folder ${sourceBranch} failed to list sub-files and sub-directories.")
        for (subfile in subfiles) {
            val subprocessing = ProcessingFile().apply {
                operation = folder.operation
                sourcePathname = subfile.absolutePath
                destinationPathname = destinationBranch.resolve(subfile.name).absolutePath
                isRegularFile = subfile.isFile
                isFolder = subfile.isDirectory
            }
            if (subprocessing.isFolder == true) {
                // TODO: Maybe propagate failures?
                this.backupFolder(subprocessing)
            } else
            if (subprocessing.isRegularFile == true) {
                // TODO: Maybe propagate failures?
                this.backupFile(subprocessing)
            } else {
                // TODO: A special file was found. Add it to the list and return a Partial later.
            }
        }

        // TODO: For now it only creates the destination root folder.
        return Result(ResultStatus.Success)
    }

    override fun backupFile(file: ProcessingFile): Result {
        TODO("Not yet implemented")
    }

}