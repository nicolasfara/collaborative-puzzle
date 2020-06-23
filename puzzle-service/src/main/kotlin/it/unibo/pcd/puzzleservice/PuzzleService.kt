package it.unibo.pcd.puzzleservice

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val LOG: Logger = LoggerFactory.getLogger("verticle-launcher")
    val vertx = Vertx.vertx()

    fun deployVerticle(verticleClassName: String, opt: DeploymentOptions = DeploymentOptions()) {
        vertx.deployVerticle(verticleClassName, opt) {
            if (!it.succeeded()) {
                LOG.error("$verticleClassName deploy failed: ${it.cause()}", it.cause())
            } else {
                LOG.info("$verticleClassName deploy successful")
            }
        }
    }
    with(DeploymentOptions()) {
        instances = 1
        deployVerticle("it.unibo.pcd.puzzleservice.verticles.WebServerVerticle")
    }
}

