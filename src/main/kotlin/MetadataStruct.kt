
// This class represents file/folder attributes, provided to/from backends.
class MetadataStruct {
    var size: Long = 0L
    var modificationTime: Long = 0L
    var posixPermissions: String = ""
    var isSymbolicLink: Boolean = false

    fun isSameAs (other: MetadataStruct): Boolean {
        return this.size == other.size &&
                this.modificationTime == other.modificationTime &&
                this.posixPermissions == other.posixPermissions
    }

}