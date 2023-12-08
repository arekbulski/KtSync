import java.time.Duration

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

fun suffixedThroughput (s: Double): String {
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

fun timeToHMS (d: Duration): String {
    val hh = d.seconds / 3600
    val mm = (d.seconds % 3600) / 60
    val ss = d.seconds % 60
    return "%02d:%02d:%02d".format(hh, mm, ss)
}
