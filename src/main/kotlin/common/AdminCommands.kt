package common
import com.xenomachina.argparser.ArgParser

class AdminCommands(parser: ArgParser) {
    val approve by parser.flagging(
        "-a", "--approve",
        help = "Approve post"
    )

    val muteUser by parser.storing(
        "--muteuser",
        help = "Mute User"
    )

    val unMuteUser by parser.storing(
        "--unmuteuser",
        help = "Unmute User"
    )

    val muteTag by parser.storing(
        "--mutetag",
        help = "Mute Tag"
    )

    val unMuteTag by parser.storing(
    "--unmutetag",
    help = "Unmute Tag"
    )

    val subscribe by parser.storing(
        "--subscribe",
        help = "Subscribe"
    )

    val unsubscribe by parser.storing(
        "--unsubscribe",
        help = "Unsubscribe"
    )

}