
open class PartiallyFailedException (
    description: String? = null,
    cause: Exception? = null,
) : Exception(
    description,
    cause,
)