import org.junit.jupiter.api.Test
import kotlin.time.DurationUnit
import kotlin.time.measureTime
import kotlin.time.toDuration

class ExceptionsTest {

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