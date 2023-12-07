
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
