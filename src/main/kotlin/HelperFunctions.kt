import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// This function transforms number of bytes to a string eg. 5000 -> "5.0 KB".
fun suffixedFileSize (n: Long): String {
    if (n < 1000L)
        return "$n B"
    if (n < 1000L*1000L)
        return "${"%.1f".format(n/1000.0)} KB"
    if (n < 1000L*1000L*1000L)
        return "${"%.1f".format(n/1000.0/1000.0)} MB"
    if (n < 1000L*1000L*1000L*1000L)
        return "${"%.1f".format(n/1000.0/1000.0/1000.0)} GB"
    return "${"%.1f".format(n/1000.0/1000.0/1000.0/1000.0)} TB"
}

// This function transforms a number of files to a string eg. 10 -> "10 files".
fun suffixedFileCount (n: Long): String {
    return "$n files"
}

// This function transforms a byte throughput to a string eg. 5000 -> "5.0 KB/sec".
fun suffixedByteThroughput (s: Double): String {
    if (s < 1000L)
        return "${"%.1f".format(s)} B/sec"
    if (s < 1000L*1000L)
        return "${"%.1f".format(s/1000.0)} KB/sec"
    if (s < 1000L*1000L*1000L)
        return "${"%.1f".format(s/1000.0/1000.0)} MB/sec"
    if (s < 1000L*1000L*1000L*1000L)
        return "${"%.1f".format(s/1000.0/1000.0/1000.0)} GB/sec"
    return "${"%.1f".format(s/1000.0/1000.0/1000.0/1000.0)} TB/sec"
}

// This function transforms a file throughput to a string eg. 10 -> "10.0 files/sec".
fun suffixedFileThroughput (s: Double): String {
    return "%.1f files/sec".format(s)
}

// This function transforms a Duration into a string eg. (Duration of 10m 59s) -> "00:10:59.000".
fun timeToHMSM (d: Duration): String {
    val hh = d.seconds / 3600
    val mm = (d.seconds % 3600) / 60
    val ss = d.seconds % 60
    val milis = d.nano / 1000000
    return "%02d:%02d:%02d.%03d".format(hh, mm, ss, milis)
}

// This function generates a current time based pathname eg. ("/file") -> "/file-trash-2023-12-31-12-00-59".
fun generateTrashPathname (pathname: String): String {
    val datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
    return "${pathname}-trash-$datetime"
}
