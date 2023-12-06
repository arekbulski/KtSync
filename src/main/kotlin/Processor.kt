
abstract class Processor {

    abstract fun backupProcess (process: ProcessingProcess)
    abstract fun backupFolder (folder: ProcessingFile)
    abstract fun backupFile (file: ProcessingFile)

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