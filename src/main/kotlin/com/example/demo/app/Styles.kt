package com.example.demo.app

import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
    }

    init {
        container {
            padding = box(10.px)
        }
        textField {
            padding = box(10.px)
            prefHeight = 50.px
            fontSize = 20.px
        }
        label {
            padding = box(10.px)
            fontSize = 30.px
            fontWeight = FontWeight.BOLD
        }
        checkBox {
            padding = box(10.px)
            fontSize = 20.px
        }
    }
}