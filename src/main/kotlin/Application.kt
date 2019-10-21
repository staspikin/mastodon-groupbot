import bot.ClosedGroupBot
import bot.GroupBot
import bot.OpenGroupBot
import bot.PremoderatedGroupBot
import com.xenomachina.argparser.ArgParser
import common.Arguments
import common.Config
import common.runMode
import java.lang.Thread.sleep


fun main(args: Array<String>) {
    val parsedArgs = ArgParser(args).parseInto(::Arguments)
    var continueRun = true
    var config = Config()

    if (parsedArgs.init) {
        config.read(parsedArgs.configFile)
        val groupbot = OpenGroupBot(config, parsedArgs.stateFile)
        groupbot.runBot(parsedArgs.init)
    }
    else {
        config.read(parsedArgs.configFile)
        val groupbot: GroupBot =
            when (parsedArgs.mode) {
                runMode.OPEN -> {
                    OpenGroupBot(config, parsedArgs.stateFile)
                }
                runMode.CLOSED -> {
                    ClosedGroupBot(config, parsedArgs.stateFile)
                }
                runMode.PREMODERATED -> {
                    PremoderatedGroupBot(config, parsedArgs.stateFile)
                }
            }
        groupbot.initialize()

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("Bye...")
                continueRun = false
            }
        })

        while (continueRun) {
            sleep(config.timer().toLong())
            println("Running Bot...")
            groupbot.runBot(parsedArgs.init)
        }
        println("Exiting")
    }
}
