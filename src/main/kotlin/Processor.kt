
abstract class Processor {

    abstract fun backupProcess (operation: ProcessingOperation): Result
    abstract fun backupFolder (folder: ProcessingFile): Result
    abstract fun backupFile (file: ProcessingFile): Result

}