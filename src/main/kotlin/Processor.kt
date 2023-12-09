
// This class is abstract, and it defines a common set of methods that the concrete processor classes implement. Note that the methods are not abstract, they all throw NotImplementedError-s. The subtypes need not override any of them.
abstract class Processor {

//----------------------------------------------------------------------------------------------------------------------

    open fun backupProcess (process: ProcessingProcess) {
        throw NotImplementedError()
    }

    open fun backupFolder (folder: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun finishFolder (folder: ProcessingFile, success: Boolean?, description: String?) {
        throw NotImplementedError()
    }

    open fun backupFile (file: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun finishFile (file: ProcessingFile, success: Boolean?, description: String?) {
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

    open fun renameTo (pathname: String, newname:String): Boolean {
        throw NotImplementedError()
    }

    open fun createFolder (pathname: String): Boolean {
        throw NotImplementedError()
    }

    open fun createRegularFile (pathname: String): Boolean {
        throw NotImplementedError()
    }

    open fun listFolderEntries (pathname: String): List<String> {
        throw NotImplementedError()
    }

    open fun getSize (pathname: String): Long {
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

//----------------------------------------------------------------------------------------------------------------------

    fun propagate (
        action: () -> Unit,
        onSuccess: (() -> Unit)? = null,
        onPartiallyFailed: ((PartiallyFailedException) -> Unit)? = null,
        onFailed: ((TotallyFailedException) -> Unit)? = null,
        onException: ((Exception) -> Unit)? = null,
    ) {
        try {
            action.invoke()
            onSuccess?.invoke()
        }
        catch (e: TotallyFailedException) {
            onFailed?.invoke(e)
        }
        catch (e: PartiallyFailedException) {
            onPartiallyFailed?.invoke(e)
        }
        catch (e: Exception) {
            onException?.invoke(e)
        }
    }

}