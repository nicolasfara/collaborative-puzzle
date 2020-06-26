package it.unibo.pcd.client.controller

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import it.unibo.pcd.client.utils.Constants

class ViewController(private val vertx: Vertx) {


    fun notifyStarted(message: JsonObject) {
        vertx.eventBus().send(Constants.CREATE_ADDRESS, message.toString())
    }

    fun notifyJoined(message: JsonObject) {
        vertx.eventBus().send(Constants.JOIN_ADDRESS, message.toString())
    }

    fun swap(message: JsonObject) {
        vertx.eventBus().send(Constants.SWAP_ADDRESS, message.toString())
    }

}