
abstract class Processor {

    abstract fun backupProcess (operation: ProcessingOperation): Result
    abstract fun backupFolder (folder: ProcessingFile): Result
    abstract fun backupFile (file: ProcessingFile): Result

    fun passthrough (action: () -> Result, onSuccess: ((Result) -> Unit)? = null, onError: ((Result) -> Unit)? = null): Result {
        try {
            val subresult = action()
            if (subresult.status == ResultStatus.Success) {
                onSuccess?.invoke(subresult)
            } else {
                onError?.invoke(subresult)
            }
            return subresult
        } catch (e: Exception) {
            onError?.invoke(Result(ResultStatus.Failure, null, e, null))
            throw e
        }
    }

}