
class FailedException (
    description: String? = null,
    cause: Exception? = null,
) : PartiallyFailedException(
    description,
    cause,
)