
// This class represents a backup/restore job (just the configuration details).
class JobDescription {

    var operation: JobOperation = JobOperation.Undefined
    var algorithm: JobAlgorithm = JobAlgorithm.Undefined

    var sourcePath: String? = null
    var destinationPath: String? = null

    var accessToken: String? = null

    override fun toString(): String {
        return "Profile: op=$operation algo=$algorithm, from $sourcePath, to $destinationPath, "+
                "accesstoken=(${if (accessToken != null) "${accessToken!!.length}" else "absent"})"
    }

}