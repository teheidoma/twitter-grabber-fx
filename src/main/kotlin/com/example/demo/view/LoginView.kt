package com.example.demo.view

import com.example.demo.LoginController
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import twitter4j.TwitterFactory
import java.awt.Desktop
import java.net.URI


class LoginView : View() {
    private val pin = SimpleStringProperty()

    val loginController: LoginController by inject()
    override val root = vbox {
        hyperlink(loginController.link) {
            action {
                Desktop.getDesktop().browse(URI.create(loginController.link.get()))
            }
        }
        textfield(pin)
        button("Login") {
            enableWhen(pin.matches(Regex("\\d{7}")))
            action {
                if (loginController.login(pin.get())) {
                    replaceWith<MainView>()
                }
            }
        }
    }


    override fun onDock() {
        try {
            if (TwitterFactory.getSingleton().verifyCredentials() != null) {
                replaceWith<MainView>()
            }
        }catch (ex:IllegalStateException){
            //ignore
        }
    }

    init {

    }
}
