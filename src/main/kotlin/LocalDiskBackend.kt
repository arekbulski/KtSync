import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.fileSize
import kotlin.io.path.pathString

// This class exposes filesystem operations is a somewhat agnostic way.
class LocalDiskBackend (subprocessor: Processor) : Passthrough(subprocessor) {

    override fun absolute (pathname: String): String {
        try {
            return File(pathname).absolutePath
        } catch (e: Exception) {
            throw TotalFailureException("Failed to evaluate absolute path $pathname.", this, e)
        }
    }

    override fun resolve (pathname: String, relative: String): String {
        try {
            return File(pathname).resolve(relative).absolutePath
        } catch (e: Exception) {
            throw TotalFailureException("Failed to evaluate resolved path $pathname with relative $relative.", this, e)
        }
    }

    override fun relative (pathname: String, base: String): String {
        try {
            return "/" + File(pathname).toRelativeString(File(base))
        } catch (e: Exception) {
            throw TotalFailureException("Failed to evaluate relative path $pathname with base $base.", this, e)
        }
    }

    override fun extractName (pathname: String): String {
        try {
            return File(pathname).name
        } catch (e: Exception) {
            throw TotalFailureException("Failed to extract name from path $pathname.", this, e)
        }
    }

    override fun exists (pathname: String): Boolean {
        try {
            return Files.exists(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to check if path exists $pathname.", this, e)
        }
    }

    override fun isRegularFile (pathname: String): Boolean {
        try {
            return Files.isRegularFile(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed at checking if a regular file $pathname.", this, e)
        }
    }

    override fun isFolder (pathname: String): Boolean {
        try {
            return Files.isDirectory(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed at checking if a folder $pathname.", this, e)
        }
    }

    override fun isSymbolicLink (pathname: String): Boolean {
        try {
            return Files.isSymbolicLink(File(pathname).toPath())
        } catch (e: Exception) {
            throw TotalFailureException("Failed at checking if a symbolic link $pathname.", this, e)
        }
    }

    override fun renameTo (pathname: String, newname: String) {
        try {
            if (! File(pathname).renameTo(File(newname)))
                throw TotalFailureException("Failed to rename $pathname into $newname.", this, null)
        } catch (e: TotalFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to rename $pathname into $newname.", this, e)
        }
    }

    // Note this method creates an empty folder atomically, it never does anything with an existing folder.
    override fun createFolder (pathname: String) {
        try {
            if(! File(pathname).mkdir())
                throw TotalFailureException("Failed to create a folder $pathname.", this, null)
        } catch (e: TotalFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create a folder $pathname.", this, e)
        }
    }

    // Note this method creates an empty file atomically, it never overwrites an existing file.
    override fun createRegularFile (pathname: String) {
        try {
            if (! File(pathname).createNewFile())
                throw TotalFailureException("Failed to create a regular file $pathname.", this, null)
        } catch (e: TotalFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create a regular file $pathname.", this, e)
        }
    }

    override fun listFolderEntries (pathname: String): List<String> {
        try {
            return File(pathname).listFiles()?.map{ it.absolutePath }
                ?: throw TotalFailureException("Failed to list entries in folder $pathname.", this, null)
        } catch (e: TotalFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to list entries in folder $pathname.", this, e)
        }
    }

    override fun getFileSize (pathname: String): Long {
        try {
            return File(pathname).toPath().fileSize()
        } catch (e: Exception) {
            throw TotalFailureException("Failed to get file size of $pathname.", this, e)
        }
    }

    override fun getModificationTime (pathname: String): FileTime {
        try {
            return Files.getLastModifiedTime(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to get file mtime of $pathname.", this, e)
        }
    }

    override fun setModificationTime (pathname: String, mtime: FileTime) {
        try {
            Files.setLastModifiedTime(File(pathname).toPath(), mtime)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to set file mtime of $pathname.", this, e)
        }
    }

    override fun getPosixPermissions (pathname: String): Set<PosixFilePermission> {
        try {
            return Files.getPosixFilePermissions(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to get file mtime of $pathname.", this, e)
        }
    }

    override fun setPosixPermissions (pathname: String, permissions: Set<PosixFilePermission>) {
        try {
            Files.setPosixFilePermissions(File(pathname).toPath(), permissions)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to get file mtime of $pathname.", this, e)
        }
    }

    @ExperimentalUnsignedTypes
    override fun readFileContent (pathname: String): UByteArray {
        try {
            return Files.readAllBytes(File(pathname).toPath()).asUByteArray()
        } catch (e: Exception) {
            throw TotalFailureException("Failed to read content of file $pathname.", this, e)
        }
    }

    // Note this method creates an empty file atomically, it never overwrites an existing file.
    @ExperimentalUnsignedTypes
    override fun writeFileContent (pathname: String, data: UByteArray) {
        try {
            Files.write(File(pathname).toPath(), data.asByteArray(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create/write ${data.size} bytes content into file $pathname.", this, e)
        }
    }

    // Note this method creates an empty file atomically, it never overwrites an existing file.
    // Note that [sourcePath] always refers to existing file, and [destinationPath] is always created.
    override fun copyFileProgressively (sourcePath: String, destinationPath: String, onUpdate: (Long) -> Unit, onSuccess: () -> Unit, onFailure: () -> Unit) {
        try {
            // This method throws a TotalFailureException.
            this.createRegularFile(destinationPath)

            propagateCombined({
                // TODO: Reuse the buffer across calls. Or make array smaller, but at maximum 1MiB.
                val buffer = ByteArray(1*1024*1024)
                var progress = 0L
                FileInputStream(File(sourcePath)).use { inputStream ->
                    FileOutputStream(File(destinationPath)).use { outputStream ->
                        while (true) {
                            val amount = inputStream.read(buffer)
                            if (amount < 0)
                                break
                            outputStream.write(buffer, 0, amount)
                            progress += amount
                            onUpdate.invoke(progress)
                        }
                    }
                }
            }, null, {
                // TODO: Trash a partially copied file?
                throw TotalFailureException("Failed to stream copy content from file $sourcePath to file $destinationPath", this, it)
            })

            propagateCombined({
                val mtime = this.getModificationTime(sourcePath)
                this.setModificationTime(destinationPath, mtime)
            }, null, {
                throw PartialFailureException("Could not get/set mtime from file $sourcePath to file $destinationPath.", this, it)
            })
            propagateCombined({
                val permissions = this.getPosixPermissions(sourcePath)
                this.setPosixPermissions(destinationPath, permissions)
            }, null, {
                throw PartialFailureException("Could not get/set permissions from file $sourcePath to file $destinationPath.", this, it)
            })

            onSuccess.invoke()
        } catch (e: Exception) {
            onFailure.invoke()
            throw e
        }
    }

    // Note this method creates an empty file atomically, it never overwrites an existing file.
    // Note that [sourcePath] always refers to existing file, and [destinationPath] is always created.
    override fun cloneFile(sourcePath: String, destinationPath: String) {
        try {
            Files.createLink(File(destinationPath).toPath(), File(sourcePath).toPath())

            propagateCombined({
                val mtime = this.getModificationTime(sourcePath)
                this.setModificationTime(destinationPath, mtime)
            }, null, {
                throw PartialFailureException("Could not get/set mtime from file $sourcePath to file $destinationPath.", this, it)
            })
            propagateCombined({
                val permissions = this.getPosixPermissions(sourcePath)
                this.setPosixPermissions(destinationPath, permissions)
            }, null, {
                throw PartialFailureException("Could not get/set permissions from file $sourcePath to file $destinationPath.", this, it)
            })
        } catch (e: PartialFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create a hardlink from target $sourcePath to $destinationPath.", this, e)
        }
    }

    // Note this method creates a symbolic link atomically, it never overwrites an existing file.
    // Note that [sourcePath] always refers to existing file, and [destinationPath] is always created.
    override fun copySymbolicLink (sourcePath: String, destinationPath: String) {
        try {
            val target = Files.readSymbolicLink(File(sourcePath).toPath()).pathString
            Files.createSymbolicLink(File(destinationPath).toPath(), File(target).toPath())
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create symlink from target $sourcePath to $destinationPath", this, e)
        }
    }

}