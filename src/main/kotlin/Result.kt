
class Result (
    val status: ResultStatus,
    val description: String? = null,
    val exception: Exception? = null,
    val processor: Processor? = null,
) {

    override fun toString(): String {
        return "$status" + (if (description != null) " ($description)" else "") +
            ((if (exception != null) " caused by $exception" else "")) +
            ((if (processor != null) " thrown by $processor" else ""))
    }

}