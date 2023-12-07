import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightMagenta

class Debug (
    subprocessor: Processor
) : Passthrough (subprocessor) {

    override fun backupProcess(process: ProcessingProcess) {
        val terminal = process.terminal!!
        terminal.println((brightMagenta)("(debug) into backupProcess ($process)"))
        subprocessor.backupProcess(process)
        if (subprocessor !is DoNothing)
            terminal.println((brightMagenta)("(debug) out of backupProcess ($process)"))
    }

    override fun backupFolder(folder: ProcessingFile) {
        val terminal = folder.process?.terminal!!
        terminal.println((brightMagenta)("(debug) into backupFolder ($folder)"))
        subprocessor.backupFolder(folder)
        if (subprocessor !is DoNothing)
            terminal.println((brightMagenta)("(debug) out of backupFolder ($folder)"))
    }

    override fun backupFile(file: ProcessingFile) {
        val terminal = file.process?.terminal!!
        terminal.println((brightMagenta)("(debug) into backupFile ($file)"))
        subprocessor.backupFile(file)
        if (subprocessor !is DoNothing)
            terminal.println((brightMagenta)("(debug) out of backupFile ($file)"))
    }

}