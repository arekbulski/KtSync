import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission

// This class is abstract, and it defines a common set of methods that the concrete processor classes implement. Note that the methods are not abstract, they all throw NotImplementedError-s. The subtypes need not override any of them.
abstract class Processor {

//----------------------------------------------------------------------------------------------------------------------

    open fun backupProcess (process: ProcessingProcess) {
        throw NotImplementedError()
    }

    open fun backupFolder (folder: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun initFolderProgress (folder: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun finishFolderProgress (folder: ProcessingFile, result: Exception?) {
        throw NotImplementedError()
    }

    open fun backupFile (file: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun initFileProgress (file: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun updateFileProgress (file: ProcessingFile, progress: Long) {
        throw NotImplementedError()
    }

    open fun finishFileProgress(file: ProcessingFile, result: Exception?) {
        throw NotImplementedError()
    }

    open fun backupSymbolicLink (symlink: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun initSymbolicLinkProgress (symlink: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun finishSymbolicLinkProgress (symlink: ProcessingFile, result: Exception?) {
        throw NotImplementedError()
    }

//----------------------------------------------------------------------------------------------------------------------

    open fun estimateFolder (process: ProcessingProcess, folder: String) {
        throw NotImplementedError()
    }

    open fun initEstimationProgress (process: ProcessingProcess) {
        throw NotImplementedError()
    }

    open fun updateEstimationProgress (process: ProcessingProcess) {
        throw NotImplementedError()
    }

    open fun finishEstimationProgress (process: ProcessingProcess) {
        throw NotImplementedError()
    }

//----------------------------------------------------------------------------------------------------------------------

    open fun absolute (pathname: String): String {
        throw NotImplementedError()
    }

    open fun resolve (pathname: String, relative: String): String {
        throw NotImplementedError()
    }

    open fun relative (pathname: String, root: String): String {
        throw NotImplementedError()
    }

    open fun extractName (pathname: String): String {
        throw NotImplementedError()
    }

    open fun exists (pathname: String): Boolean {
        throw NotImplementedError()
    }

    open fun isRegularFile (pathname: String): Boolean {
        throw NotImplementedError()
    }

    open fun isFolder (pathname: String): Boolean {
        throw NotImplementedError()
    }

    open fun isSymbolicLink (pathname: String): Boolean {
        throw NotImplementedError()
    }

    open fun renameTo (pathname: String, newname:String) {
        throw NotImplementedError()
    }

    open fun createFolder (pathname: String) {
        throw NotImplementedError()
    }

    open fun createRegularFile (pathname: String) {
        throw NotImplementedError()
    }

    open fun listFolderEntries (pathname: String): List<String> {
        throw NotImplementedError()
    }

    open fun getSize (pathname: String): Long {
        throw NotImplementedError()
    }

    open fun getModificationTime (pathname: String): FileTime {
        throw NotImplementedError()
    }

    open fun setModificationTime (pathname: String, mtime: FileTime) {
        throw NotImplementedError()
    }

    open fun getPosixPermissions (pathname: String): Set<PosixFilePermission> {
        throw NotImplementedError()
    }

    open fun setPosixPermissions (pathname: String, permissions: Set<PosixFilePermission>) {
        throw NotImplementedError()
    }

    @ExperimentalUnsignedTypes
    open fun readFileContent (pathname: String): UByteArray {
        throw NotImplementedError()
    }

    @ExperimentalUnsignedTypes
    open fun writeFileContent (pathname: String, data: UByteArray) {
        throw NotImplementedError()
    }

    open fun copyFileProgressively (sourcePath: String, destinationPath: String, onUpdate: (Long) -> Unit, onSuccess: () -> Unit, onFailure: () -> Unit ) {
        throw NotImplementedError()
    }

    open fun copySymbolicLink (sourcePath: String, destinationPath: String) {
        throw NotImplementedError()
    }

//----------------------------------------------------------------------------------------------------------------------

    fun propagateArms (action: () -> Unit, onSuccess: (() -> Unit)? = null, onPartialFailure: ((PartialFailureException) -> Unit)? = null, onTotalFailure: ((TotalFailureException) -> Unit)? = null, onException: ((Exception) -> Unit)? = null) {
        try {
            action.invoke()
            onSuccess?.invoke()
        }
        catch (e: TotalFailureException) {
            onTotalFailure?.invoke(e)
        }
        catch (e: PartialFailureException) {
            onPartialFailure?.invoke(e)
        }
        catch (e: Exception) {
            onException?.invoke(e)
        }
    }

    fun propagateCombined (action: () -> Unit, onSuccess: (() -> Unit)? = null, onException: ((Exception) -> Unit)? = null) {
        try {
            action.invoke()
            onSuccess?.invoke()
        }
        catch (e: Exception) {
            onException?.invoke(e)
        }
    }

}