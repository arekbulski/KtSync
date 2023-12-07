import com.github.ajalt.mordant.terminal.Terminal

class ProcessingProcess {

    var profile: Profile? = null
    var terminal: Terminal? = null
    var successfulEntries: Long = 0
    var successfulBytes: Long = 0
    var failedEntriesCount: Long = 0
    var failedEntries = mutableMapOf<String,String>()

    override fun toString(): String {
        return "ProcessingProcess: $profile"
    }

}