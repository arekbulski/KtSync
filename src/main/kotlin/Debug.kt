import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightMagenta

// This class is a passthrough class, it prints the arguments/results between the processor above and processor below it. It does not catch any exceptions.
// TODO: The NothingImplemented processor below it throws exceptions, so this class is now defunct. However it could still be used higher in the processor chain.
class Debug (subprocessor: Processor) : Passthrough (subprocessor) {

    val EnableDebugPrinting = false

    override fun backupProcess(process: ProcessingProcess) {
        val terminal = process.terminal!!
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) into backupProcess ($process)"))
        subprocessor.backupProcess(process)
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) out of backupProcess ($process)"))
    }

    override fun backupFolder(folder: ProcessingFile) {
        val terminal = folder.process?.terminal!!
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) into backupFolder ($folder)"))
        subprocessor.backupFolder(folder)
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) out of backupFolder ($folder)"))
    }

    override fun backupFile(file: ProcessingFile) {
        val terminal = file.process?.terminal!!
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) into backupFile ($file)"))
        subprocessor.backupFile(file)
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) out of backupFile ($file)"))
    }

}