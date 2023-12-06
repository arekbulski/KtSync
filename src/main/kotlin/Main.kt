
fun main(args: Array<String>) {

    var subprocessor: Processor = DoNothing()
    subprocessor = Debug(subprocessor)
    var main = MainRunner(subprocessor)

}