
class FailedException(
    override val description: String? = null,
    override val causedBy: Exception? = null,
    override val thrownBy: Processor? = null,
) : PartiallyFailedException(
    description,
    causedBy,
    thrownBy,
)