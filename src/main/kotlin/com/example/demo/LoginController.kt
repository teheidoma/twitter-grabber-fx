package com.example.demo

import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken

const val CONSUMER_KEY = "7Lr8A9PEWprzhZLKsYO5C8y6S"
const val CONSUMER_SECRET = "rrO5obfVyuBs5zQlOe86hY3cbADD1D4BZzQj2ohYt4S3MjDMNz"

class LoginController : Controller() {
    val link = SimpleStringProperty()

    private var requestToken: RequestToken? = null

    private var twitter: Twitter = TwitterFactory.getSingleton()


    init {
        val token = config.string("token") as String?
        val tokenSecret = config.string("secret") as String?
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET)

        if (token != null && tokenSecret != null) {
            val accessToken = AccessToken(token, tokenSecret)
            twitter.oAuthAccessToken = accessToken
            try {
                twitter.verifyCredentials()
                println("auth")
            } catch (ex: Exception) {
                requestToken = twitter.oAuthRequestToken

                link.set(requestToken!!.authorizationURL)
            }
        } else {
            requestToken = twitter.oAuthRequestToken

            link.set(requestToken!!.authorizationURL)
        }
    }

    fun login(code: String): Boolean {
        return try {
            if (requestToken != null) {
                val token = twitter.getOAuthAccessToken(requestToken, code)

                config["token"] = token.token
                config["secret"] = token.tokenSecret

                config.save()
                true
            } else {
                false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }
}