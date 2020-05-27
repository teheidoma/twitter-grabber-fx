package com.example.demo.view

import com.example.demo.MainController
import com.example.demo.twitterRegex
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Paths

class MainView : View("Twitter-grabber") {
    private val mainController: MainController by inject()

    override val root = borderpane {
        top {
            vbox {
                textfield(mainController.url) {
                    enableWhen {
                        mainController.running.not()
                    }
                }
                hbox {
                    alignment = Pos.CENTER
                    checkbox("only video", mainController.onlyVideo)
                    checkbox("skip re-tweets", mainController.includeRetweets)
                    checkbox("skip duplicates", mainController.skipDuplicates)
                }
            }
        }
        center {
            separator(Orientation.HORIZONTAL)
            vbox {
                textflow {
                    alignment = Pos.CENTER

                    label("total: ")
                    label(mainController.total)
                }

            }
        }
        bottom {
            hbox {
                hyperlink("http://twitter.com/teheidoma") {
                    action {
                        Desktop.getDesktop().browse(URI("http://twitter.com/teheidoma"))
                    }
                    alignment = Pos.CENTER_LEFT
                }
                buttonbar {
                    alignment = Pos.CENTER_RIGHT
                    button("start") {
                        enableWhen {
                            mainController.url.matches(twitterRegex).and(mainController.running.not())
                        }
                        action {
                            mainController.start()
                        }
                    }
                    button("cancel") {
                        enableWhen {
                            mainController.running
                        }
                        action {
                            mainController.cancel()
                        }
                    }
                }
            }
        }
    }
}