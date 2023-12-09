import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightMagenta

class Debug (
    subprocessor: Processor
) : Passthrough (subprocessor) {

    val EnableDebugPrinting = false

    override fun backupProcess(process: ProcessingProcess) {
        val terminal = process.terminal!!
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) into backupProcess ($process)"))
        subprocessor.backupProcess(process)
        if (EnableDebugPrinting)
            if (subprocessor !is NothingImplemented)
                terminal.println((brightMagenta)("(debug) out of backupProcess ($process)"))
    }

    override fun backupFolder(folder: ProcessingFile) {
        val terminal = folder.process?.terminal!!
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) into backupFolder ($folder)"))
        subprocessor.backupFolder(folder)
        if (EnableDebugPrinting)
            if (subprocessor !is NothingImplemented)
                terminal.println((brightMagenta)("(debug) out of backupFolder ($folder)"))
    }

    override fun backupFile(file: ProcessingFile) {
        val terminal = file.process?.terminal!!
        if (EnableDebugPrinting)
            terminal.println((brightMagenta)("(debug) into backupFile ($file)"))
        subprocessor.backupFile(file)
        if (EnableDebugPrinting)
            if (subprocessor !is NothingImplemented)
                terminal.println((brightMagenta)("(debug) out of backupFile ($file)"))
    }

}