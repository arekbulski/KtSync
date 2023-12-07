
fun main(args: Array<String>) {

    var subprocessor: Processor = DoNothing()
    subprocessor = Debug(subprocessor)
    subprocessor = LocalDiskBackend(subprocessor)
    subprocessor = PrettyPrintFiles(subprocessor)
    subprocessor = FullBackupAlgorithm(subprocessor)
    subprocessor = PrettyPrintAlgorithm(subprocessor)
    var runner = MainRunner(subprocessor)
    runner.main()

}