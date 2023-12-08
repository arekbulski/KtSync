import com.github.ajalt.mordant.animation.ProgressAnimation
import com.github.ajalt.mordant.terminal.Terminal
import java.time.LocalDateTime

class ProcessingProcess {

    var profile: Profile? = null
    var terminal: Terminal? = null
    var progressbar: ProgressAnimation? = null

    var estimatedCount: Long = 0
    var estimatedBytes: Long = 0
    var processedCount: Long = 0
    var processedBytes: Long = 0
    var successfulCount: Long = 0
    var successfulBytes: Long = 0
    var failedEntries = mutableMapOf<String,Exception>()

    var destinationRenamedTo: String? = null
    var timeBegun: LocalDateTime? = null
    var timeEnded: LocalDateTime? = null

}