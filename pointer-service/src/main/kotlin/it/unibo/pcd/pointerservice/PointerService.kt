package it.unibo.pcd.pointerservice

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

fun main() {
    val logger: Logger = LoggerFactory.getLogger("verticle-launcher")
    val vertx = Vertx.vertx()

    fun deployVerticle(verticleClassName: String, opt: DeploymentOptions = DeploymentOptions()) {
        vertx.deployVerticle(verticleClassName, opt) {
            if (!it.succeeded()) {
                logger.error("$verticleClassName deploy failed: ${it.cause()}", it.cause())
            } else {
                logger.info("$verticleClassName deploy successful")
            }
        }
    }
    with(DeploymentOptions()) {
        instances = 2
        deployVerticle("it.unibo.pcd.pointerservice.verticles.PointerVerticle")
        deployVerticle("it.unibo.pcd.pointerservice.verticles.DataStoreVerticle")
    }

}

