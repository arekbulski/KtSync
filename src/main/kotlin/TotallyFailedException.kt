
class TotallyFailedException(
    override val description: String? = null,
    override val thrownBy: Processor? = null,
    override val causedBy: Exception? = null,
) : PartiallyFailedException(
    description,
    thrownBy,
    causedBy,
)