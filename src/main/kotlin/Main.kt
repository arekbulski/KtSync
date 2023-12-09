
fun main(args: Array<String>) {

    var subprocessor: Processor = NothingImplemented()
    // TODO: As stated in Debug class comment, inserting it here is useless.
    subprocessor = Debug(subprocessor)
    subprocessor = LocalDiskBackend(subprocessor)
    subprocessor = PrettyPrintFiles(subprocessor)
    subprocessor = FullBackupAlgorithm(subprocessor)
    subprocessor = PrettyPrintAlgorithm(subprocessor)
    var runner = MainRunner(subprocessor)
    runner.run()

}