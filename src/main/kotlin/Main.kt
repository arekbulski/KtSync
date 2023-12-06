
fun main(args: Array<String>) {

    var subprocessor: Processor = DoNothing()
    subprocessor = Debug(subprocessor)
    subprocessor = FullBackupAlgorithm(subprocessor)
    subprocessor = PrettyPrintAlgorithm(subprocessor)
    var main = MainRunner(subprocessor)
    main.main()

}