import com.github.ajalt.mordant.terminal.Terminal

class ProcessingProcess {

    var profile: Profile? = null
    var terminal: Terminal? = null
    var failedEntries: Int = 0
    var failedBytes: Int = 0

    override fun toString(): String {
        return "ProcessingProcess: $profile"
    }

}