import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardOpenOption

class LocalDiskBackend (
    val subprocessor: Processor
) : Processor() {

    override fun backupProcess (process: ProcessingProcess) {
        subprocessor.backupProcess(process)
    }

    override fun backupFolder (folder: ProcessingFile) {
        subprocessor.backupFolder(folder)
    }

    override fun backupFile (file: ProcessingFile) {
        subprocessor.backupFile(file)
    }

    override fun canonical (pathname: String): String {
        // TODO: Needs experimental verification.
        if (isSymbolicLink(pathname))
            throw FailedException("Pathname $pathname is a symbolic link.")
        return File(pathname).canonicalPath
    }

    override fun resolve(pathname: String, relative: String): String {
        // TODO: Maybe absolutepath ?
        return File(pathname).resolve(relative).canonicalPath
    }

    override fun extractName(pathname: String): String {
        return File(pathname).name
    }

    override fun exists (pathname: String): Boolean {
        return Files.exists(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
    }

    override fun isRegularFile(pathname: String): Boolean {
        return Files.isRegularFile(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
    }

    override fun isDirectory(pathname: String): Boolean {
        return Files.isDirectory(File(pathname).toPath(), LinkOption.NOFOLLOW_LINKS)
    }

    override fun isSymbolicLink (pathname: String): Boolean {
        return Files.isSymbolicLink(File(pathname).toPath())
    }

    override fun renameTo(pathname: String, newname: String): Boolean {
        return File(pathname).renameTo(File(newname))
    }

    override fun createFolder(pathname: String): Boolean {
        return File(pathname).mkdir()
    }

    override fun createRegularFile(pathname: String): Boolean {
        return File(pathname).createNewFile()
    }

    override fun listFolderEntries(pathname: String): List<String> {
        // TODO: Maybe absolutepath ?
        return File(pathname).listFiles()?.map{ it.canonicalPath }
            ?: throw FailedException("Folder $pathname failed to list entries.")
    }

    @ExperimentalUnsignedTypes
    override fun readFileContent (pathname: String): UByteArray {
        return Files.readAllBytes(File(pathname).toPath()).asUByteArray()
    }

    @ExperimentalUnsignedTypes
    override fun writeFileContent(pathname: String, data: UByteArray) {
        Files.write(File(pathname).toPath(), data.asByteArray(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
    }

}