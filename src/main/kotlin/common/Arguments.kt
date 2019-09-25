package common
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class Arguments(parser: ArgParser) {
    val init by parser.flagging(
        "-i", "--init",
        help = "Initialize bot and save notifications to state"
    )

    val configFile by parser.storing(
        "-c", "--config",
        help = "Configuration File"
    ).default("config.json")

    val stateFile by parser.storing(
        "-s", "--state",
        help = "State File"
    ).default("state.json")

    val mode by parser.mapping(
        "--open" to runMode.OPEN,
        "--closed" to runMode.CLOSED,
        "--premoderated" to runMode.PREMODERATED,
        help = "Mode of operation").default(runMode.OPEN)


}

