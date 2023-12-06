
class ProcessingFile {

    var operation: ProcessingOperation? = null
    var name: String = "???"
    var pathname: String = "???"
    var isRegularFile: Boolean = false
    var isFolder: Boolean = false
    var data: UByteArray = UByteArray(0)
    var result: Result = Result(ResultStatus.Failure, "???")

}