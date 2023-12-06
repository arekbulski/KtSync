// TODO: Maybe write a toString?

class FailedException (
    description: String? = null,
    cause: Exception? = null,
    processor: Processor? = null,
) : PartiallyFailedException(
    description,
    cause,
    processor,
)