package it.unibo.pcd.puzzleservice.verticles

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import org.slf4j.LoggerFactory

class WebserverVerticle : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(WebserverVerticle::class.java)
    private lateinit var rabbitMQClient: RabbitMQClient

    override fun start(startPromise: Promise<Void>?) {
        logger.info("${WebserverVerticle::class.java} started")
        val config = RabbitMQOptions()
        config.uri = "amqp://guest:guest@localhost"
        rabbitMQClient = RabbitMQClient.create(vertx, config)
        rabbitMQClient.start {
            if (it.succeeded()) {
                logger.info("Connection to rabbitmq succeeded")
            } else {
                logger.error("Failed to connect to rabbitmq")
            }
        }
    }

    override fun stop(stopPromise: Promise<Void>?) {
        logger.info("${WebserverVerticle::class.java} stopped")
    }
}