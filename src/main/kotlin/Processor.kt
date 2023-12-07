import java.io.File

abstract class Processor {

    open fun backupProcess (process: ProcessingProcess) {
        throw NotImplementedError()
    }

    open fun backupFolder (folder: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun backupFile (file: ProcessingFile) {
        throw NotImplementedError()
    }

    open fun canonical (pathname: String): String {
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

    open fun isDirectory (pathname: String): Boolean {
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

    @ExperimentalUnsignedTypes
    open fun readFileContent (pathname: String): UByteArray {
        throw NotImplementedError()
    }

    @ExperimentalUnsignedTypes
    open fun writeFileContent (pathname: String, data: UByteArray) {
        throw NotImplementedError()
    }

    fun passthrough (action: () -> Unit,
                     onSuccess: (() -> Unit)? = null,
                     onPartiallyFailed: ((Exception) -> Unit)? = null,
                     onFailed: ((Exception) -> Unit)? = null,
                     onException: ((Exception) -> Unit)? = null, ) {
        try {
            action.invoke()
            onSuccess?.invoke()
        }
        catch (e: FailedException) {
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