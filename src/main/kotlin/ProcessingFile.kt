
class ProcessingFile {

    var process: ProcessingProcess? = null
    var sourcePathname: String? = null
    var destinationPathname: String? = null
    var isRoot: Boolean? = null
    var isRegularFile: Boolean? = null
    var isFolder: Boolean? = null

    override fun toString(): String {
        return "ProcessingFile: from $sourcePathname, to $destinationPathname, " +
                "root=$isRoot, regular=$isRegularFile, folder=$isFolder"
    }

}