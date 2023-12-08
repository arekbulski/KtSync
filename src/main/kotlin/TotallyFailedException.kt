
class TotallyFailedException(
    override val description: String? = null,
    override val thrownBy: Processor? = null,
) : PartiallyFailedException(
    description,
    thrownBy,
)