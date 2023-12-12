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

// This class exposes filesystem operations in a somewhat agnostic way.
class LocalDiskBackend (subprocessor: Processor) : Passthrough(subprocessor) {

//----------------------------------------------------------------------------------------------------------------------

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

//----------------------------------------------------------------------------------------------------------------------

    override fun existsLocal (pathname: String): Boolean {
        try {
            return Files.exists(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to check if path exists $pathname.", this, e)
        }
    }

    override fun isRegularFileLocal (pathname: String): Boolean {
        try {
            return Files.isRegularFile(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed at checking if a regular file $pathname.", this, e)
        }
    }

    override fun isFolderLocal (pathname: String): Boolean {
        try {
            return Files.isDirectory(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed at checking if a folder $pathname.", this, e)
        }
    }

    override fun isSymbolicLinkLocal (pathname: String): Boolean {
        try {
            return Files.isSymbolicLink(File(pathname).toPath())
        } catch (e: Exception) {
            throw TotalFailureException("Failed at checking if a symbolic link $pathname.", this, e)
        }
    }

    override fun listFolderEntriesLocal (pathname: String): List<String> {
        try {
            return File(pathname).listFiles()?.map{ it.absolutePath }
                ?: throw TotalFailureException("Failed to list entries in folder $pathname.", this, null)
        } catch (e: TotalFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to list entries in folder $pathname.", this, e)
        }
    }

    override fun getFileSizeLocal (pathname: String): Long {
        try {
            return File(pathname).toPath().fileSize()
        } catch (e: Exception) {
            throw TotalFailureException("Failed to get file size of $pathname.", this, e)
        }
    }

    override fun getModificationTimeLocal (pathname: String): FileTime {
        try {
            return Files.getLastModifiedTime(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to get file mtime of $pathname.", this, e)
        }
    }

    override fun getPosixPermissionsLocal (pathname: String): Set<PosixFilePermission> {
        try {
            return Files.getPosixFilePermissions(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
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

//----------------------------------------------------------------------------------------------------------------------

    override fun encodeNameRemote (name: String): String {
        return name
    }

    override fun existsRemote (pathname: String): Boolean {
        return this.existsLocal(pathname)
    }

    override fun renameToRemote (pathname: String, newname: String) {
        try {
            if (! File(pathname).renameTo(File(newname)))
                throw TotalFailureException("Failed to rename from $pathname into $newname.", this, null)
        } catch (e: TotalFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to rename from $pathname into $newname.", this, e)
        }
    }

    // Note this method creates an empty folder atomically, it never does anything with an existing folder.
    override fun createFolderRemote (pathname: String) {
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
    override fun createRegularFileRemote (pathname: String) {
        try {
            if (! File(pathname).createNewFile())
                throw TotalFailureException("Failed to create a regular file $pathname.", this, null)
        } catch (e: TotalFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create a regular file $pathname.", this, e)
        }
    }

    override fun getFileSizeRemote (pathname: String): Long {
        return this.getFileSizeLocal(pathname)
    }

    override fun getModificationTimeRemote (pathname: String): FileTime {
        return this.getModificationTimeLocal(pathname)
    }

    override fun setModificationTimeRemote (pathname: String, mtime: FileTime) {
        try {
            Files.setLastModifiedTime(File(pathname).toPath(), mtime)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to set file mtime of $pathname.", this, e)
        }
    }

    override fun getPosixPermissionsRemote(pathname: String): Set<PosixFilePermission> {
        return this.getPosixPermissionsLocal(pathname)
    }

    override fun setPosixPermissionsRemote (pathname: String, permissions: Set<PosixFilePermission>) {
        try {
            Files.setPosixFilePermissions(File(pathname).toPath(), permissions)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to get file mtime of $pathname.", this, e)
        }
    }

    // Note this method creates an empty file atomically, it never overwrites an existing file.
    // Note that [sourcePath] always refers to existing file, and [destinationPath] is always created.
    override fun copyFileProgressivelyRemote (sourcePath: String, destinationPath: String, onUpdate: (Long) -> Unit, onSuccess: () -> Unit, onFailure: () -> Unit) {
        try {
            // This method throws a TotalFailureException.
            this.createRegularFileRemote(destinationPath)

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
                val mtime = this.getModificationTimeLocal(sourcePath)
                this.setModificationTimeRemote(destinationPath, mtime)
            }, null, {
                throw PartialFailureException("Could not get/set mtime from file $sourcePath to file $destinationPath.", this, it)
            })
            propagateCombined({
                val permissions = this.getPosixPermissionsLocal(sourcePath)
                this.setPosixPermissionsRemote(destinationPath, permissions)
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
    override fun cloneFileRemote (sourcePath: String, destinationPath: String) {
        try {
            Files.createLink(File(destinationPath).toPath(), File(sourcePath).toPath())

            propagateCombined({
                val mtime = this.getModificationTimeLocal(sourcePath)
                this.setModificationTimeRemote(destinationPath, mtime)
            }, null, {
                throw PartialFailureException("Could not get/set mtime from file $sourcePath to file $destinationPath.", this, it)
            })
            propagateCombined({
                val permissions = this.getPosixPermissionsLocal(sourcePath)
                this.setPosixPermissionsRemote(destinationPath, permissions)
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
    override fun copySymbolicLinkRemote (sourcePath: String, destinationPath: String) {
        try {
            val target = Files.readSymbolicLink(File(sourcePath).toPath()).pathString
            Files.createSymbolicLink(File(destinationPath).toPath(), File(target).toPath())
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create symlink from target $sourcePath to $destinationPath", this, e)
        }
    }

//----------------------------------------------------------------------------------------------------------------------

}