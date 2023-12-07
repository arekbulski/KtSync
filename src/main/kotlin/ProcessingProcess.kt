import com.github.ajalt.mordant.terminal.Terminal

class ProcessingProcess {

    var profile: Profile? = null
    var terminal: Terminal? = null
    var successfulEntries: Long = 0
    var successfulBytes: Long = 0
    var failedEntries: Long = 0

    override fun toString(): String {
        return "ProcessingProcess: $profile"
    }

}