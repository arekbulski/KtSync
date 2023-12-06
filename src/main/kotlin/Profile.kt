
class Profile {

    var operation: String? = null
    var algorithm: String? = null
    var sourcePath: String? = null
    var destinationPath: String? = null

    override fun toString(): String {
        return "Profile: $operation $algorithm, from $sourcePath, to $destinationPath"
    }

}