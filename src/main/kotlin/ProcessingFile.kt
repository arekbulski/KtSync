
// This class represents a backup/restore item (a file, directory, symbolic link, even unknown types).
class ProcessingFile {

    var process: ProcessingJob? = null

    var sourcePath: String? = null
    var destinationPath: String? = null
    var previousPath: String? = null
    var relativePath: String? = null

    var isRoot: Boolean = false

    var isFolder: Boolean = false
    var isRegularFile: Boolean = false
    var isSymbolicLink: Boolean = false
    var size: Long = 0L

    override fun toString(): String {
        return "ProcessingFile: relativepath=$relativePath ; " +
            "root=$isRoot, regular=$isRegularFile, symlink=$isSymbolicLink, folder=$isFolder, " +
            "size=$size (${suffixedFileSize(size)})"
    }

}