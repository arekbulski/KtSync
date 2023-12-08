
open class PartiallyFailedException(
    open val description: String? = null,
    open val thrownBy: Processor? = null,
    open val causedBy: Exception? = null,
) : Exception(
    description,
) {

    override fun toString(): String {
        return "${this::class.simpleName} ($description)" +
                (if (thrownBy != null) " (thrown by ${thrownBy!!::class.simpleName})" else "") +
                (if (causedBy != null) " <-- $causedBy" else "")
    }

}