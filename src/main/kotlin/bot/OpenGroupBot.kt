package bot

import common.Config

class OpenGroupBot(config: Config, stateFile: String) : GroupBot(config, stateFile) {
    override fun parseNotifications(init: Boolean) {
        notifications.asReversed().forEach {
            when (it.type) {
                "follow" -> {
                    println("New follower ${it.account!!.acct}. Welcome!")
                    if ((!init) and (it.account!!.acct !in state.mutedUsers)) {
                        this.sendPublicMessage(config.welcomeMessage() + " @" + it.account!!.acct)
                    }
                }
                "mention" -> {
                    if ((it.status!!.inReplyToId == null) and (it.status!!.visibility == "public")) {
                        if ((it.account!!.acct in followers) and (it.account!!.acct !in state.mutedUsers)
                            and (!isMutedTagInList(it.status!!.tags))
                        ) {
                            println("New mention from follower ${it.account!!.acct}. Boosting!")
                            if (!init) {
                                this.boost(it.status!!)
                            }
                        } else {
                            println("New mention from not follower ${it.account!!.acct}. Sent intro message.")
                            if (!init) {
                                this.sendPrivateMessage(
                                    it.status!!,
                                    "@" + it.account!!.acct + " " + config.introMessage() + " " + config.admins()
                                )
                            }
                        }
                    }
                    if ((it.account!!.acct in config.adminList()) and (it.status!!.visibility == "private")) {
                        println("New command " + it.status!!.content + " from admin " + it.status!!.account + ". Processing!")
                        processAdminCommands(it.status!!)
                    }
                }
                else -> {
                }
            }
            state.notifications.add(it.id)
            state.store(stateFile)
        }
    }
}