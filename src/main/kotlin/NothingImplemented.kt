
// This class used to bounce back recursive calls from higher processors. Now it just throws NotImplementedError on every occasion.
class NothingImplemented : Processor() {

    // This needs to bounce.
    override fun backupProcess(process: ProcessingJob) {
    }

}