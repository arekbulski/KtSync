import java.nio.file.attribute.FileTime

abstract class Passthrough(
    val subprocessor: Processor,
) : Processor() {

    override fun backupProcess(process: ProcessingProcess) {
        subprocessor.backupProcess(process)
    }

    override fun backupFolder(folder: ProcessingFile) {
        subprocessor.backupFolder(folder)
    }

    override fun initFolderProgress(folder: ProcessingFile) {
        subprocessor.initFolderProgress(folder)
    }

    override fun finishFolderProgress(folder: ProcessingFile, result: Exception?) {
        subprocessor.finishFolderProgress(folder, result)
    }

    override fun backupFile(file: ProcessingFile) {
        subprocessor.backupFile(file)
    }

    override fun initFileProgress(file: ProcessingFile) {
        subprocessor.initFileProgress(file)
    }

    override fun updateFileProgress(file: ProcessingFile, progress: Long) {
        subprocessor.updateFileProgress(file, progress)
    }

    override fun finishFileProgress(file: ProcessingFile, result: Exception?) {
        subprocessor.finishFileProgress(file, result)
    }

    override fun backupSymbolicLink(symlink: ProcessingFile) {
        subprocessor.backupSymbolicLink(symlink)
    }

    override fun initSymbolicLinkProgress(symlink: ProcessingFile) {
        subprocessor.initSymbolicLinkProgress(symlink)
    }

    override fun finishSymbolicLinkProgress(symlink: ProcessingFile, result: Exception?) {
        subprocessor.finishSymbolicLinkProgress(symlink, result)
    }

    override fun estimateFolder(process: ProcessingProcess, folder: String) {
        subprocessor.estimateFolder(process, folder)
    }

    override fun initEstimationProgress(process: ProcessingProcess) {
        subprocessor.initEstimationProgress(process)
    }

    override fun updateEstimationProgress(process: ProcessingProcess) {
        subprocessor.updateEstimationProgress(process)
    }

    override fun finishEstimationProgress(process: ProcessingProcess) {
        subprocessor.finishEstimationProgress(process)
    }

    override fun absolute(pathname: String): String {
        return subprocessor.absolute(pathname)
    }

    override fun resolve(pathname: String, relative: String): String {
        return subprocessor.resolve(pathname, relative)
    }

    override fun relative(pathname: String, base: String): String {
        return subprocessor.relative(pathname, base)
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

    override fun isFolder(pathname: String): Boolean {
        return subprocessor.isFolder(pathname)
    }

    override fun isSymbolicLink(pathname: String): Boolean {
        return subprocessor.isSymbolicLink(pathname)
    }

    override fun renameTo(pathname: String, newname: String) {
        return subprocessor.renameTo(pathname, newname)
    }

    override fun createFolder(pathname: String) {
        return subprocessor.createFolder(pathname)
    }

    override fun createRegularFile(pathname: String) {
        return subprocessor.createRegularFile(pathname)
    }

    override fun listFolderEntries(pathname: String): List<String> {
        return subprocessor.listFolderEntries(pathname)
    }

    override fun getSize(pathname: String): Long {
        return subprocessor.getSize(pathname)
    }

    override fun getModificationTime(pathname: String): FileTime {
        return subprocessor.getModificationTime(pathname)
    }

    override fun setModificationTime(pathname: String, mtime: FileTime) {
        subprocessor.setModificationTime(pathname, mtime)
    }

    @ExperimentalUnsignedTypes
    override fun readFileContent(pathname: String): UByteArray {
        return subprocessor.readFileContent(pathname)
    }

    @ExperimentalUnsignedTypes
    override fun writeFileContent(pathname: String, data: UByteArray) {
        subprocessor.writeFileContent(pathname, data)
    }

    override fun copyFileProgressively(sourcePath: String, destinationPath: String, onUpdate: (Long) -> Unit, onSuccess: () -> Unit, onFailure: () -> Unit) {
        subprocessor.copyFileProgressively(sourcePath, destinationPath, onUpdate, onSuccess, onFailure)
    }

    override fun copySymbolicLink(sourcePath: String, destinationPath: String) {
        subprocessor.copySymbolicLink(sourcePath, destinationPath)
    }

}