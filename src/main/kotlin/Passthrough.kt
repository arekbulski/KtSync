import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission

abstract class Passthrough(val subprocessor: Processor) : Processor() {

//----------------------------------------------------------------------------------------------------------------------

    override fun backupProcess(process: ProcessingJob) {
        subprocessor.backupProcess(process)
    }

    override fun backupFolder(folder: ProcessingFile) {
        subprocessor.backupFolder(folder)
    }

    override fun backupFile(file: ProcessingFile) {
        subprocessor.backupFile(file)
    }

    override fun backupSymbolicLink(symlink: ProcessingFile) {
        subprocessor.backupSymbolicLink(symlink)
    }

//----------------------------------------------------------------------------------------------------------------------

    override fun initFolderProgress(folder: ProcessingFile) {
        subprocessor.initFolderProgress(folder)
    }

    override fun finishFolderProgress(folder: ProcessingFile, result: Exception?) {
        subprocessor.finishFolderProgress(folder, result)
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

    override fun initSymbolicLinkProgress(symlink: ProcessingFile) {
        subprocessor.initSymbolicLinkProgress(symlink)
    }

    override fun finishSymbolicLinkProgress(symlink: ProcessingFile, result: Exception?) {
        subprocessor.finishSymbolicLinkProgress(symlink, result)
    }

//----------------------------------------------------------------------------------------------------------------------

    override fun estimateFolder(process: ProcessingJob, folder: String) {
        subprocessor.estimateFolder(process, folder)
    }

//----------------------------------------------------------------------------------------------------------------------

    override fun initEstimationProgress(process: ProcessingJob) {
        subprocessor.initEstimationProgress(process)
    }

    override fun updateEstimationProgress(process: ProcessingJob) {
        subprocessor.updateEstimationProgress(process)
    }

    override fun finishEstimationProgress(process: ProcessingJob) {
        subprocessor.finishEstimationProgress(process)
    }

//----------------------------------------------------------------------------------------------------------------------

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

//----------------------------------------------------------------------------------------------------------------------

    override fun existsLocal(pathname: String): Boolean {
        return subprocessor.existsLocal(pathname)
    }

    override fun isRegularFileLocal(pathname: String): Boolean {
        return subprocessor.isRegularFileLocal(pathname)
    }

    override fun isFolderLocal(pathname: String): Boolean {
        return subprocessor.isFolderLocal(pathname)
    }

    override fun isSymbolicLinkLocal(pathname: String): Boolean {
        return subprocessor.isSymbolicLinkLocal(pathname)
    }

    override fun listFolderEntriesLocal(pathname: String): List<String> {
        return subprocessor.listFolderEntriesLocal(pathname)
    }

    override fun getMetadataLocal(pathname: String): MetadataStruct {
        return subprocessor.getMetadataLocal(pathname)
    }

    override fun getFileSizeLocal(pathname: String): Long {
        return subprocessor.getFileSizeLocal(pathname)
    }

    override fun getModificationTimeLocal(pathname: String): FileTime {
        return subprocessor.getModificationTimeLocal(pathname)
    }

    override fun getPosixPermissionsLocal(pathname: String): Set<PosixFilePermission> {
        return subprocessor.getPosixPermissionsLocal(pathname)
    }

    @ExperimentalUnsignedTypes
    override fun readFileContent(pathname: String): UByteArray {
        return subprocessor.readFileContent(pathname)
    }

    @ExperimentalUnsignedTypes
    override fun writeFileContent(pathname: String, data: UByteArray) {
        subprocessor.writeFileContent(pathname, data)
    }

//----------------------------------------------------------------------------------------------------------------------

    override fun encodeNameRemote(name: String): String {
        return subprocessor.encodeNameRemote(name)
    }

    override fun existsRemote(pathname: String): Boolean {
        return subprocessor.existsRemote(pathname)
    }

    override fun renameToRemote(pathname: String, newname: String) {
        return subprocessor.renameToRemote(pathname, newname)
    }

    override fun createFolderRemote(pathname: String) {
        return subprocessor.createFolderRemote(pathname)
    }

    override fun createRegularFileRemote(pathname: String) {
        return subprocessor.createRegularFileRemote(pathname)
    }

    override fun getMetadataRemote(pathname: String): MetadataStruct {
        return subprocessor.getMetadataRemote(pathname)
    }

    override fun setMetadataRemote(pathname: String, metadata: MetadataStruct) {
        subprocessor.setMetadataRemote(pathname, metadata)
    }

    override fun getFileSizeRemote(pathname: String): Long {
        return subprocessor.getFileSizeRemote(pathname)
    }

    override fun getModificationTimeRemote(pathname: String): FileTime {
        return subprocessor.getModificationTimeRemote(pathname)
    }

    override fun setModificationTimeRemote(pathname: String, mtime: FileTime) {
        subprocessor.setModificationTimeRemote(pathname, mtime)
    }

    override fun getPosixPermissionsRemote(pathname: String): Set<PosixFilePermission> {
        return subprocessor.getPosixPermissionsRemote(pathname)
    }

    override fun setPosixPermissionsRemote(pathname: String, permissions: Set<PosixFilePermission>) {
        subprocessor.setPosixPermissionsRemote(pathname, permissions)
    }

    override fun copyFileProgressivelyRemote(sourcePath: String, destinationPath: String, onUpdate: (Long) -> Unit, onSuccess: () -> Unit, onFailure: () -> Unit) {
        subprocessor.copyFileProgressivelyRemote(sourcePath, destinationPath, onUpdate, onSuccess, onFailure)
    }

    override fun cloneFileRemote(sourcePath: String, destinationPath: String) {
        subprocessor.cloneFileRemote(sourcePath, destinationPath)
    }

    override fun copySymbolicLinkRemote(sourcePath: String, destinationPath: String) {
        subprocessor.copySymbolicLinkRemote(sourcePath, destinationPath)
    }

//----------------------------------------------------------------------------------------------------------------------

}