import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileSize

class LocalDiskBackend (
    subprocessor: Processor
) : Passthrough(subprocessor) {

    override fun absolute (pathname: String): String {
        try {
            return File(pathname).absolutePath
        } catch (e: Exception) {
            throw TotalFailureException("Failed to evaluate absolute path $pathname.", this, e)
        }
    }

    override fun resolve(pathname: String, relative: String): String {
        try {
            return File(pathname).resolve(relative).absolutePath
        } catch (e: Exception) {
            throw TotalFailureException("Failed to evaluate resolved path $pathname with relative $relative.", this, e)
        }
    }

    override fun relative(pathname: String, base: String): String {
        try {
            return "/" + File(pathname).toRelativeString(File(base))
        } catch (e: Exception) {
            throw TotalFailureException("Failed to evaluate relative path $pathname with base $base.", this, e)
        }
    }

    override fun extractName(pathname: String): String {
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

    override fun isRegularFile(pathname: String): Boolean {
        try {
            return Files.isRegularFile(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to check if a regular file $pathname.", this, e)
        }
    }

    override fun isFolder(pathname: String): Boolean {
        try {
            return Files.isDirectory(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to check if a directory $pathname.", this, e)
        }
    }

    override fun isSymbolicLink (pathname: String): Boolean {
        try {
            return Files.isSymbolicLink(File(pathname).toPath())
        } catch (e: Exception) {
            throw TotalFailureException("Failed to check if a symbolic link $pathname.", this, e)
        }
    }

    override fun renameTo(pathname: String, newname: String): Boolean {
        try {
            return File(pathname).renameTo(File(newname))
        } catch (e: Exception) {
            throw TotalFailureException("Failed to rename $pathname into $newname.", this, e)
        }
    }

    override fun createFolder(pathname: String): Boolean {
        try {
            return File(pathname).mkdir()
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create a folder $pathname.", this, e)
        }
    }

    override fun createRegularFile(pathname: String): Boolean {
        try {
            return File(pathname).createNewFile()
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create a regular file $pathname.", this, e)
        }
    }

    override fun listFolderEntries(pathname: String): List<String> {
        try {
            return File(pathname).listFiles()?.map{ it.absolutePath }
                ?: throw TotalFailureException("Failed to list entries in folder $pathname.", this, null)
        } catch (e: TotalFailureException) {
            throw e
        } catch (e: Exception) {
            throw TotalFailureException("Failed to list entries in folder $pathname.", this, e)
        }
    }

    override fun getSize(pathname: String): Long {
        try {
            return File(pathname).toPath().fileSize()
        } catch (e: Exception) {
            throw TotalFailureException("Failed to get file size of $pathname.", this, e)
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

    @ExperimentalUnsignedTypes
    override fun writeFileContent(pathname: String, data: UByteArray) {
        try {
            Files.write(File(pathname).toPath(), data.asByteArray(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
        } catch (e: Exception) {
            throw TotalFailureException("Failed to create/write ${data.size} bytes content into file $pathname.", this, e)
        }
    }

}