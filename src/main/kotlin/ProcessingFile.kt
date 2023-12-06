
class ProcessingFile {

    var process: ProcessingProcess? = null
    var sourcePath: String? = null
    var destinationPath: String? = null
    var isRoot: Boolean? = null
    var isRegularFile: Boolean? = null
    var isFolder: Boolean? = null

    override fun toString(): String {
        return "ProcessingFile: from $sourcePath, to $destinationPath, " +
                "root=$isRoot, regular=$isRegularFile, folder=$isFolder"
    }

}