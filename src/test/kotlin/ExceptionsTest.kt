import org.junit.jupiter.api.Test
import kotlin.time.DurationUnit
import kotlin.time.measureTime
import kotlin.time.toDuration

class ExceptionsTest {

    @Test
    fun testToString() {
        val e1 = FailedException("Description", null, null)
        assert(e1.toString() == "FailedException (Description) caused by null thrown by null")
        val e2 = PartiallyFailedException("Description", null, null)
        assert(e2.toString() == "PartiallyFailedException (Description) caused by null thrown by null")
        val e3 = FailedException("TBA", Exception("TODO"), null)
        assert(e3.toString() == "FailedException (TBA) caused by java.lang.Exception: TODO thrown by null")
    }

    private fun callForException() {
        throw NotImplementedError()
    }

    private fun callForResult(): Boolean {
        return true
    }

    @Test
    fun timeitExceptions() {
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