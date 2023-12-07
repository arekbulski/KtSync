import com.github.ajalt.mordant.terminal.Terminal
import java.time.LocalDateTime

class ProcessingProcess {

    var profile: Profile? = null
    var terminal: Terminal? = null
    var successfulEntries: Long = 0
    var successfulBytes: Long = 0
    var failedEntriesCount: Long = 0
    var failedEntries = mutableMapOf<String,String>()
    var destinationRenamedTo: String? = null
    var timeBegun: LocalDateTime? = null
    var timeEnded: LocalDateTime? = null

    override fun toString(): String {
        return "ProcessingProcess: $profile"
    }

}