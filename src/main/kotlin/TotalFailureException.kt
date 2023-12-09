
class TotalFailureException(
    override val description: String? = null,
    override val thrownBy: Processor? = null,
    override val causedBy: Exception? = null,
) : PartialFailureException(
    description,
    thrownBy,
    causedBy,
)