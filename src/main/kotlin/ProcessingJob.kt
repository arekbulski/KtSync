import com.github.ajalt.mordant.animation.ProgressAnimation
import com.github.ajalt.mordant.terminal.Terminal
import java.time.LocalDateTime

// This class represents a backup/restore job with all the configuration details, terminal and progress bar instance, statistics, time measurements, etc.
class ProcessingJob {

    var jobDescription: JobDescription? = null

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
    var processingBegun: LocalDateTime? = null
    var processingEnded: LocalDateTime? = null

    // TODO: Add toString implementation.

}