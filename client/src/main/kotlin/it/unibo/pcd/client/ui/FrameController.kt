package it.unibo.pcd.client.ui

import javax.swing.JFrame

class FrameController: JFrame() {
    init {
        title = "Collaborative puzzle"
        isResizable = false
        defaultCloseOperation = EXIT_ON_CLOSE
    }
}