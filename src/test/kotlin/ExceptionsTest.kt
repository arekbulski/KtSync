import org.junit.jupiter.api.Test
import kotlin.time.DurationUnit
import kotlin.time.measureTime
import kotlin.time.toDuration

class ExceptionsTest {

    @Test
    fun testToString() {
        val e1 = TotalFailureException("Description")
        assert(e1.toString() == "TotalFailureException (Description)")
        val e2 = PartialFailureException("Description")
        assert(e2.toString() == "PartialFailureException (Description)")

        val exception1 = Exception("Message")
        val nested1 = TotalFailureException("Description", NothingImplemented(), exception1)
        assert(nested1.toString() == "TotalFailureException (Description) (thrown by NothingImplemented) <-- java.lang.Exception: Message")
    }

    private fun callForException() {
        throw NotImplementedError()
    }

    private fun callForResult(): Boolean {
        return true
    }

    @Test
    fun timeitExceptionVsResult() {
        val heavyExceptionTook = measureTime {
            try {
                callForException()
            }
            catch (e: NotImplementedError) {
            }
        }
        val resultTook = measureTime {
            val result = callForResult()
            if (! result)
                throw NotImplementedError()
        }
        assert(resultTook < heavyExceptionTook/10.0)
        assert(heavyExceptionTook < 0.5.toDuration(DurationUnit.MILLISECONDS))
    }

}