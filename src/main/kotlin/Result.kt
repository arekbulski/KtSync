
class Result (
    val status: ResultStatus,
    val description: String? = null,
    val exception: Exception? = null,
) {

    override fun toString(): String {
        return "$status${if (description != null) " ($description)" else ""}" +
                "${if (exception != null) " caused by $exception" else ""}"
    }

}