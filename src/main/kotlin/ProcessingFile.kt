
// This class represents a backup/restore item (a file, directory, symbolic link, even unknown types).
class ProcessingFile {

    var process: ProcessingProcess? = null

    var sourcePath: String? = null
    var destinationPath: String? = null

    var isRoot: Boolean = false
    var isRegularFile: Boolean = false
    var isSymbolicLink: Boolean = false
    var isFolder: Boolean = false
    var size: Long = 0L

    override fun toString(): String {
        return "ProcessingFile: from $sourcePath, to $destinationPath, " +
            "root=$isRoot, regular=$isRegularFile, symlink=$isSymbolicLink, folder=$isFolder, " +
            "size=$size (~${suffixedSize(size)})"
    }

}