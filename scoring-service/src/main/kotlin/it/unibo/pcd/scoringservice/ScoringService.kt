package it.unibo.pcd.scoringservice


import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
        instances = 1
        deployVerticle("it.unibo.pcd.scoringservice.verticles.ScoringVerticle")
        deployVerticle("it.unibo.pcd.scoringservice.verticles.DataScoreVerticle")
    }
}
