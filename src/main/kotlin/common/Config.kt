package common

import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.api.method.Apps
import okhttp3.OkHttpClient
import java.io.File

class Config {
    private lateinit var host:           String
    private lateinit var name:           String
    private lateinit var website:        String
    private lateinit var clientId:       String
    private lateinit var authCode:       String
    private lateinit var redirectUri:    String
    private lateinit var clientSecret:   String
    private lateinit var accessToken:    String
    private var admins: MutableList<String> = mutableListOf()
    private lateinit var introMessage:   String
    private lateinit var welcomeMessage: String
    private var timer:                   Int = 0

    fun read (filename: String = "config.json" ) {
        try {
            val config = Gson().fromJson(File(filename).readText(), Config ::class.java)
            this.host = config.host
            this.name = config.name
            this.website = config.website
            this.clientId = config.clientId
            this.authCode = config.authCode
            this.redirectUri = config.redirectUri
            this.clientSecret = config.clientSecret
            this.accessToken = config.accessToken
            this.introMessage = config.introMessage
            this.welcomeMessage = config.welcomeMessage
            this.admins = config.admins
            this.timer = config.timer
        }
        catch (e: Exception) {
            buildInteractive()
            store(filename)
        }
    }

    private fun store (filename: String = "config.json" )
    {
        File(filename).writeText(Gson().toJson(this))
    }

    fun buildInteractive(filename: String = "config.json") {
        println("Initialising configuration")
        println("Enter Instance name:")
        this.host = readLine()!!

        val client: MastodonClient = MastodonClient.Builder(this.host, OkHttpClient.Builder(), Gson()).build()
        val apps = Apps(client)

        println("")
        println("Registering app")
        println("Enter client name:")
        this.name = readLine()!!

        println("Enter website:")
        this.website = readLine()!!

        val appRegistration = apps.createApp(
            clientName = name,
            redirectUris = "urn:ietf:wg:oauth:2.0:oob",
            scope = Scope(Scope.Name.ALL),
            website = website
        ).execute()
        this.clientId = appRegistration.clientId
        val url = apps.getOAuthUrl(clientId, Scope(Scope.Name.ALL))

        println("")
        println("Authenticate app using this URL")
        println(url)
        println("")
        println("Enter auth code:")
        this.authCode = readLine()!!

        this.redirectUri = appRegistration.redirectUri
        this.clientSecret = appRegistration.clientSecret
        this.accessToken = apps.getAccessToken(clientId, clientSecret,
            redirectUri, authCode, "authorization_code")
            .execute().accessToken

        println("Enter Welcome message:")
        this.welcomeMessage = readLine()!!

        println("Enter Intro message")
        this.introMessage = readLine()!!

        println("Enter Bot Admins")
        val admin = readLine()!!
        this.admins.add(admin)
        this.store(filename)
    }

    fun host(): String {
        return host
    }

    fun accessToken(): String {
        return accessToken
    }

    fun welcomeMessage(): String {
        return welcomeMessage
    }

    fun introMessage(): String {
        return introMessage
    }

    fun admins(): String {
        var _return = ""
        admins.forEachIndexed { index, s ->
            if (index != admins.lastIndex) {
                _return = _return + "@" + s + ", "
            } else {
                _return = _return + "@" + s
            }
        }
        return _return
    }

    fun adminList(): MutableList<String> {
        return admins
    }

    fun timer(): Int {
        return timer
    }

}