package it.unibo.pcd.client

import io.vertx.core.Vertx
import it.unibo.pcd.client.controller.ViewController
import it.unibo.pcd.client.ui.InitialFrame
import it.unibo.pcd.client.verticles.StartupVerticle

fun main() {

    val vertx = Vertx.vertx()
    val controller = ViewController(vertx);
    val uiController = InitialFrame(controller, vertx)
    val verticle = StartupVerticle(uiController)
    vertx.deployVerticle(verticle)
    uiController.setSize(600, 300)
    uiController.isVisible = true
}