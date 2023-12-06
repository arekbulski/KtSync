import org.junit.jupiter.api.Test
import java.io.IOException

class ResultTest {

    @Test
    fun testToString() {
        val r1 = Result(ResultStatus.Success)
        assert(r1.toString() == "Success")
        val r2 = Result(ResultStatus.Partial, "unknown issue")
        assert(r2.toString() == "Partial (unknown issue)")
        val r3 = Result(ResultStatus.Failure, "I/O", IOException())
        assert(r3.toString() == "Failure (I/O) caused by java.io.IOException")
    }

}