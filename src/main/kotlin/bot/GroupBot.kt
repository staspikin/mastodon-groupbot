package bot

import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Notification
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.api.entity.Tag
import com.sys1yagi.mastodon4j.api.method.Accounts
import com.sys1yagi.mastodon4j.api.method.Notifications
import com.sys1yagi.mastodon4j.api.method.Statuses
import com.xenomachina.argparser.ArgParser
import common.AdminCommands
import common.Config
import common.State
import okhttp3.OkHttpClient

abstract class GroupBot(config: Config, stateFile: String) {
    var client: MastodonClient
    var account: Account
    var state = State()
    var notifications: MutableList<Notification> = mutableListOf()
    var followers: MutableList<String> = mutableListOf()
    var following: MutableList<String> = mutableListOf()
    val stateFile: String = stateFile
    val config: Config = config

    init {
        state.read(stateFile)
        client = MastodonClient.Builder(config.host(), OkHttpClient.Builder(), Gson())
            .accessToken(config.accessToken())
            .build()
        account = Accounts(client).getVerifyCredentials().execute()
    }

    open fun initialize() {
    }

    private fun readNewNotifications() {
        var isInState = false

        var notificationsRequest = Notifications(client).getNotifications().execute()
        while ((notificationsRequest.link != null) and (!isInState)) {
            notificationsRequest.part.forEach {
                if (it.id !in state.notifications) {
                    if (it !in notifications) notifications.add(it)
                } else {
                    isInState = true
                }
            }
            notificationsRequest = Notifications(client).getNotifications(notificationsRequest.nextRange()).execute()
        }
    }

    private fun readFollowers() {
        var followersRequest = Accounts(client).getFollowers(account.id).execute()
        while (followersRequest.link != null) {
            followersRequest.part.forEach {
                if (it.acct !in followers) followers.add(it.acct)
            }
            followersRequest = Accounts(client).getFollowers(account.id, followersRequest.nextRange()).execute()
        }
    }

    fun readFollowing() {
        var followingRequest = Accounts(client).getFollowing(account.id).execute()
        while (followingRequest.link != null) {
            followingRequest.part.forEach {
                if (it.acct !in following) following.add(it.acct)
            }
            followingRequest = Accounts(client).getFollowing(account.id, followingRequest.nextRange()).execute()
        }
    }

    fun sendPrivateMessage(status: Status?, message: String) {
        Statuses(client).postStatus(
            message,
            status?.id, null, false, null, Status.Visibility.Private
        ).execute()
    }

    fun boost(status: Status) {
        if (!status.isReblogged) Statuses(client).postReblog(status.id).execute()
    }

    fun sendPublicMessage(message: String) {
        Statuses(client).postStatus(message, null, null, false, null).execute()
    }

    fun isMutedTagInList(tagList: List<Tag>): Boolean {
        var isTagInList = false
        tagList.forEach {
            if (it.name in state.mutedTags) isTagInList = true
        }
        return isTagInList
    }

    open fun parseNotifications(init: Boolean) {

    }

    open fun runBot(init: Boolean) {
        readNewNotifications()
        readFollowers()
        parseNotifications(init)
    }

    fun processAdminCommands(status: Status) {
        val commands: Array<String> = arrayOf(status.content)
        val parsedCommands = ArgParser(commands).parseInto(::AdminCommands)

        if (parsedCommands.muteUser.isNotEmpty()) {
            state.mutedUsers.add(parsedCommands.muteUser)
        }
        if (parsedCommands.unMuteUser.isNotEmpty()) {
            state.mutedUsers.remove(parsedCommands.unMuteUser)
        }
        if (parsedCommands.muteTag.isNotEmpty()) {
            state.mutedTags.add(parsedCommands.muteTag)
        }
        if (parsedCommands.unMuteTag.isNotEmpty()) {
            state.mutedTags.remove(parsedCommands.unMuteTag)
        }

        if (parsedCommands.subscribe.isNotEmpty()) {
            val followAccount: Account = Accounts(client).getAccountSearch(parsedCommands.subscribe, 1).execute()[0]
            Accounts(client).postFollow(followAccount.id)
            following.clear()
            this.readFollowing()
        }

        if (parsedCommands.unsubscribe.isNotEmpty()) {
            val unFollowAccount: Account = Accounts(client).getAccountSearch(parsedCommands.unsubscribe, 1).execute()[0]
            Accounts(client).postUnFollow(unFollowAccount.id)
            following.clear()
            this.readFollowing()
        }

        if (parsedCommands.approve) {
            val boostStatus =
                Statuses(client).getStatus(Statuses(client).getStatus(status.inReplyToId!!).execute().inReplyToId!!)
                    .execute()
            boost(boostStatus)
        }

        state.store(stateFile)
    }
}