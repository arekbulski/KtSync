abstract class Passthrough(
    val subprocessor: Processor,
) : Processor() {

    override fun backupProcess(process: ProcessingProcess) {
        subprocessor.backupProcess(process)
    }

    override fun backupFolder(folder: ProcessingFile) {
        subprocessor.backupFolder(folder)
    }

    override fun backupFile(file: ProcessingFile) {
        subprocessor.backupFile(file)
    }

    override fun canonical(pathname: String): String {
        return subprocessor.canonical(pathname)
    }

    override fun resolve(pathname: String, relative: String): String {
        return subprocessor.resolve(pathname, relative)
    }

    override fun extractName(pathname: String): String {
        return subprocessor.extractName(pathname)
    }

    override fun exists(pathname: String): Boolean {
        return subprocessor.exists(pathname)
    }

    override fun isRegularFile(pathname: String): Boolean {
        return subprocessor.isRegularFile(pathname)
    }

    override fun isDirectory(pathname: String): Boolean {
        return subprocessor.isDirectory(pathname)
    }

    override fun isSymbolicLink(pathname: String): Boolean {
        return subprocessor.isSymbolicLink(pathname)
    }

    override fun renameTo(pathname: String, newname: String): Boolean {
        return subprocessor.renameTo(pathname, newname)
    }

    override fun createFolder(pathname: String): Boolean {
        return subprocessor.createFolder(pathname)
    }

    override fun createRegularFile(pathname: String): Boolean {
        return subprocessor.createRegularFile(pathname)
    }

    override fun listFolderEntries(pathname: String): List<String> {
        return subprocessor.listFolderEntries(pathname)
    }

    @ExperimentalUnsignedTypes
    override fun readFileContent(pathname: String): UByteArray {
        return subprocessor.readFileContent(pathname)
    }

    @ExperimentalUnsignedTypes
    override fun writeFileContent(pathname: String, data: UByteArray) {
        subprocessor.writeFileContent(pathname, data)
    }
}