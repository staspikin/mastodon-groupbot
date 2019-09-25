package common

import com.google.gson.Gson
import java.io.File

class State {
    var notifications: MutableList<Long> = mutableListOf()
    var mutedUsers: MutableList<String> = mutableListOf()
    var mutedTags: MutableList<String> = mutableListOf()

    fun read (filename: String = "state.json" ) {
        try {
            val stateFile = Gson().fromJson(File(filename).readText(), State ::class.java)
            this.notifications = stateFile.notifications
            this.mutedTags = stateFile.mutedTags
            this.mutedUsers = stateFile.mutedUsers
        }
        catch (e: Exception) {
        }
    }

    fun store (filename: String = "state.json" )
    {
        File(filename).writeText(Gson().toJson(this))
    }
}