import java.time.Duration

// This function transforms number of bytes to a string eg. 5000 -> "5.0 KB".
fun suffixedSize (n: Long): String {
    if (n < 1024L)
        return "$n bytes"
    if (n < 1024*1024L)
        return "${"%.1f".format(n/1024.0)} KB"
    if (n < 1024*1024*1024L)
        return "${"%.1f".format(n/1024.0/1024.0)} MB"
    if (n < 1024*1024*1024*1024L)
        return "${"%.1f".format(n/1024.0/1024.0/1024.0)} GB"
    return "${"%.1f".format(n/1024.0/1024.0/1024.0/1024.0)} TB"
}

// This function transforms a number of files to a string eg. 10 -> "10 files".
fun suffixedCount (n: Long): String {
    return "$n files"
}

// This function transforms a byte throughput to a string eg. 5000 -> "5.0 KB/sec".
fun suffixedByteThroughput (s: Double): String {
    if (s < 1024L)
        return "$s bytes/sec"
    if (s < 1024*1024L)
        return "${"%.1f".format(s/1024.0)} KB/sec"
    if (s < 1024*1024*1024L)
        return "${"%.1f".format(s/1024.0/1024.0)} MB/sec"
    if (s < 1024*1024*1024*1024L)
        return "${"%.1f".format(s/1024.0/1024.0/1024.0)} GB/sec"
    return "${"%.1f".format(s/1024.0/1024.0/1024.0/1024.0)} TB/sec"
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
