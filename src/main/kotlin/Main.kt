
fun main(args: Array<String>) {

    var subprocessor: Processor = DoNothing()
    subprocessor = Debug(subprocessor)
    subprocessor = PrettyPrintAlgorithm(subprocessor)
    var main = MainRunner(subprocessor)
    main.main()

}