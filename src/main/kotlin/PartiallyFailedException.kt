
open class PartiallyFailedException (
    open val description: String? = null,
    open val thrownBy: Processor? = null,
) : Exception(
    description,
) {

    override fun toString(): String {
        return "${this::class.simpleName} ($description) thrown by $thrownBy"
    }

}