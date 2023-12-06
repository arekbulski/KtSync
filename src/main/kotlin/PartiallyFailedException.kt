// TODO: Maybe write a toString?

open class PartiallyFailedException (
    description: String? = null,
    cause: Exception? = null,
    processor: Processor? = null,
) : Exception(
    description,
    cause,
)