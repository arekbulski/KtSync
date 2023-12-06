import org.junit.jupiter.api.Test
import kotlin.time.DurationUnit
import kotlin.time.measureTime
import kotlin.time.toDuration

class ExceptionsTest {

    private fun callForException (level:Int) {
        if (level > 1)
            return callForException(level-1)
        else
            throw NotImplementedError()
    }

//    class CheapException : Throwable(null, null, false, false)
//
//    private fun callForCheapException (level:Int) {
//        if (level > 1)
//            return callForCheapException(level-1)
//        else
//            throw CheapException()
//    }

    private fun callForResult (level:Int): Boolean {
        if (level > 1)
            return callForResult(level-1)
        else
            return true
    }

    @Test
    fun timeitExceptions() {
        val heavyExceptionTook = measureTime {
            try {
                callForException(10)
            }
            catch (e: NotImplementedError) {
            }
        }
//        val cheapExceptionTook = measureTime {
//            try {
//                callForCheapException(10)
//            }
//            catch (e: CheapException) {
//            }
//        }
        val resultTook = measureTime {
            val result = callForResult(10)
            if (! result)
                throw NotImplementedError()
        }
        assert(resultTook < heavyExceptionTook/10.0)
//        assert(heavyExceptionTook < cheapExceptionTook)
        assert(heavyExceptionTook < 0.5.toDuration(DurationUnit.MILLISECONDS))
    }

}