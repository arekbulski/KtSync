import com.github.ajalt.mordant.terminal.Terminal

class ProcessingProcess {

    var profile: Profile? = null
    var terminal: Terminal? = null

    override fun toString(): String {
        return "ProcessingProcess: $profile"
    }

}