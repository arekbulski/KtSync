
// This class handles the meat, meaning it traverses the source folder, establishes file-to-file correspondence, copies everything recursively. It also calls processor below to pretty print progress bars and statuses.
open class FullBackupAlgorithm (subprocessor: Processor) : Passthrough(subprocessor) {

    // This method is called once per backup job. It, in manner of speaking, initializes it. This includes indexing the source folder.
    @ExperimentalUnsignedTypes
    override fun backupProcess(process: ProcessingJob) {
        val jobDescription = process.jobDescription!!
        val sourcePath = jobDescription.sourcePath!!
        val destinationPath = jobDescription.destinationPath!!

        // Allows other processors to initialize.
        subprocessor.backupProcess(process)

        // Indexes the entire source branch (with sub-folders) and counts files. The cumulative amount/size is stored inside the ProcessingProcess data-structure.
        subprocessor.estimateFolder(process, sourcePath)

        val root = ProcessingFile().apply {
            this.process = process
            this.sourcePath = subprocessor.absolute(sourcePath)
            this.destinationPath = subprocessor.absolute(destinationPath)
            this.previousPath = null
            this.relativePath = "/"
            this.isRoot = true
        }

        // The top-level folder gets copied recursively. Note that isRoot is true only at this point.
        this.backupFolder(root)
    }

    // This method is called once for the root folder plus once for every sub-folder in it. It calls both itself recursively for sub-folders and [backupFile] non-recursively for sub-files.
    @ExperimentalUnsignedTypes
    override fun backupFolder(folder: ProcessingFile) {
        val process = folder.process!!
        val sourcePath = folder.sourcePath!!
        val destinationPath = folder.destinationPath!!

        // This only creates an empty folder. The green status in the terminal below the item only means "folder created", as sub-folders were not created yet, sub-files were not copied yet, nor were mtime or permissions assigned yet. Note that failure to create a folder does constitute a TotalFailure, while any issues afterwards only constitute a PartialFailure.
        propagateCombined({
            subprocessor.initFolderProgress(folder)

            // Source folder checks (not just root): At the end of it, a source folder must exist and well, be a folder and not a symbolic link to a folder or anything else.
            if (! subprocessor.existsLocal(sourcePath))
                throw TotalFailureException("Source folder $sourcePath does not exist.", this)
            if (! subprocessor.isFolderLocal(sourcePath))
                throw TotalFailureException("Source folder $sourcePath is not a folder.", this)
            folder.isFolder = true

            // Destination folder checks (not just root): At the end of it, the destination folder must not exist, although in case of the root folder a rename is performed.
            if (subprocessor.existsRemote(destinationPath)) {
                if (folder.isRoot) {
                    val destinationRenamed = generateTrashPathname(destinationPath)
                    subprocessor.renameToRemote(destinationPath, destinationRenamed)
                    folder.previousPath = destinationRenamed
                    process.destinationRenamedTo = subprocessor.extractName(destinationRenamed)
                } else {
                    throw TotalFailureException("Destination folder $destinationPath already exists.", this)
                }
            }

            subprocessor.createFolderRemote(destinationPath)
        },{
            // Root folder does not count towards processed entries.
            if (! folder.isRoot)
                process.processedCount++
            subprocessor.finishFolderProgress(folder, null)
        }, {
            // Root folder does not count towards processed entries.
            if (! folder.isRoot)
                process.processedCount++
            subprocessor.finishFolderProgress(folder, it)
            throw it
        })

        // This lists sub-files and sub-folders inside a given source folder. Note that indexing (pre-estimation) that happened earlier has nothing to do with this.
        val entries = subprocessor.listFolderEntriesLocal(sourcePath)
        var failedLocally = 0L
        val previousPathOrNull = folder.previousPath
        val relativePath = folder.relativePath!!

        // Note that any failure, even a TotalFailure or unclassified exception, when copying a sub-file does not stop it's parent from attempting to copy the other entries. However, it does imply a PartialFailure at the end of this method.
        for (entryPathname in entries) {
            try {
                val entry = ProcessingFile().apply {
                    this.process = folder.process
                    this.sourcePath = entryPathname
                    this.destinationPath = subprocessor.resolve(destinationPath, subprocessor.extractName(entryPathname))
                    if (previousPathOrNull != null)
                        this.previousPath = subprocessor.resolve(previousPathOrNull, subprocessor.extractName(entryPathname))
                    this.relativePath = subprocessor.resolve(relativePath, subprocessor.extractName(entryPathname))
                    this.isRoot = false
                    this.isFolder = subprocessor.isFolderLocal(entryPathname)
                    this.isRegularFile = subprocessor.isRegularFileLocal(entryPathname)
                    this.isSymbolicLink = subprocessor.isSymbolicLinkLocal(entryPathname)
                    if (this.isRegularFile)
                        this.size = subprocessor.getFileSizeLocal(entryPathname)
                }
                if (entry.isFolder) {
                    // The sub-folders get copied recursively. Note that isRoot is false from now on.
                    this.backupFolder(entry)
                    continue
                }
                if (entry.isRegularFile) {
                    this.backupFile(entry)
                    continue
                }
                if (entry.isSymbolicLink) {
                    this.backupSymbolicLink(entry)
                    continue
                }
                throw TotalFailureException("Unknown file type $entryPathname")
            }
            // If an entry does not get copied correctly, it is listed in the [failedEntries] map. That (insertion ordered) map gets reported in Issues chapter after the backup job finished. Note that this catch clause is not limited to Partial/Total Failure.
            catch (e: Exception) {
                failedLocally++
                val entryRelativePath = subprocessor.relative(entryPathname, sourcePath)
                process.failedEntries[entryRelativePath] = e
            }
        }

        var failedAtMetadata: Exception? = null
        propagateCombined({
            val metadata = subprocessor.getMetadataLocal(sourcePath)
            subprocessor.setMetadataRemote(destinationPath, metadata)
        }, null, {
            failedAtMetadata = it
        })

        // Note that even a TotalFailure caused when copying an entry only causes a PartialFailure for it's parent directory.
        if (failedLocally > 0)
            throw PartialFailureException("Source folder $sourcePath failed to backup $failedLocally entries.", this, null)
        if (failedAtMetadata != null)
            throw PartialFailureException("Source folder $sourcePath failed to set metadata.", this, failedAtMetadata)

        // Root folder does not count towards successfully copied entries.
        if (! folder.isRoot)
            process.successfulCount++
    }

    // This method copies a single regular file. For symbolic links (that point to regular files) look elsewhere.
    // TODO: Maybe detect hardlinks in the source branch?
    @ExperimentalUnsignedTypes
    override fun backupFile(file: ProcessingFile) {
        val process = file.process!!
        val sourcePath = file.sourcePath!!
        val destinationPath = file.destinationPath!!
        val previousPathOrNull = file.previousPath

        propagateCombined({
            subprocessor.initFileProgress(file)

            // TODO: In the future, regular files will be allowed to be top-level backup objects.
            if (file.isRoot)
                throw TotalFailureException("A non-directory cannot be a top-level backup object.")

            // Source file checks: At the end of it, a source file must exist and it gets confirmed that it is a regular file and not something else.
            if (! subprocessor.existsLocal(sourcePath))
                throw TotalFailureException("Source file $sourcePath does not exist.", this)
            if (! subprocessor.isRegularFileLocal(sourcePath))
                throw TotalFailureException("Source file $sourcePath is not a regular file.", this)
            file.isRegularFile = true

            // Destination file checks: At the end of it, the destination file must not exist before the copying starts. This is to ensure that something important does not get overwritten by accident.
            if (subprocessor.existsRemote(destinationPath))
                throw TotalFailureException("Destination file $destinationPath already exists.", this)

            if (previousPathOrNull != null && this.chooseCloning(file)) {
                // File gets hardlinked (cloned).
                subprocessor.cloneFileRemote(previousPathOrNull, destinationPath)
            } else {
                // File gets stream copied, and the progress bar gets updated after every chunk.
                val progressBefore = process.processedBytes
                val progressExpectedAfter = progressBefore + file.size
                subprocessor.copyFileProgressivelyRemote(sourcePath, destinationPath,
                {
                    at -> subprocessor.updateFileProgress(file, progressBefore + at)
                }, {
                    subprocessor.updateFileProgress(file, progressExpectedAfter)
                }, {
                    subprocessor.updateFileProgress(file, progressExpectedAfter)
                    throw it
                })
            }
        },{
            // File was copied successfully. Statistics get updated, and a green status printed.
            process.processedCount++
            process.processedBytes += file.size
            process.successfulCount++
            process.successfulBytes += file.size
            subprocessor.finishFileProgress(file, null)
        }, {
            // File was not copied right. Statistics get updated (the overall progress gets incremented by entire file size), a status gets printed (yellow or red), and the exception gets propagated upwards.
            process.processedCount++
            process.processedBytes += file.size
            subprocessor.finishFileProgress(file, it)
            throw it
        })
    }

    // This method is to be overridden in [CumulativeBackupAlgorithm] class. Here it does nothing.
    open fun chooseCloning(file: ProcessingFile): Boolean {
        return false
    }

    // This method copies a symbolic link (just the target pathname).
    override fun backupSymbolicLink(symlink: ProcessingFile) {
        val process = symlink.process!!
        val sourcePath = symlink.sourcePath!!
        val destinationPath = symlink.destinationPath!!

        propagateCombined({
            subprocessor.initSymbolicLinkProgress(symlink)

            // TODO: In the future, symbolic links may be allowed to be top-level backup objects.
            if (symlink.isRoot)
                throw TotalFailureException("A non-directory cannot be a top-level backup object.")

            // Source file checks: At the end of it, a source file must exist and it gets confirmed that it is a symbolic link and not something else.
            if (! subprocessor.existsLocal(sourcePath))
                throw TotalFailureException("Source symlink $sourcePath does not exist.", this)
            if (! subprocessor.isSymbolicLinkLocal(sourcePath))
                throw TotalFailureException("Source symlink $sourcePath is not a symbolic link.", this)
            symlink.isSymbolicLink = true

            // Destination file checks: At the end of it, the destination file must not exist before the copying starts. This is to ensure that something important does not get overwritten by accident.
            if (subprocessor.existsRemote(destinationPath))
                throw TotalFailureException("Destination symlink $destinationPath already exists.", this)

            // TODO: mtime and permissions are not preserved.
            subprocessor.copySymbolicLinkRemote(sourcePath, destinationPath)
        },{
            // Symbolic link was copied successfully. Statistics get updated, and a green status printed.
            process.processedCount++
            process.successfulCount++
            subprocessor.finishSymbolicLinkProgress(symlink, null)
        }, {
            // Symbolic link was not copied right. Statistics get updated, a status gets printed (yellow or red), and the exception gets propagated upwards.
            process.processedCount++
            subprocessor.finishSymbolicLinkProgress(symlink, it)
            throw it
        })
    }

}