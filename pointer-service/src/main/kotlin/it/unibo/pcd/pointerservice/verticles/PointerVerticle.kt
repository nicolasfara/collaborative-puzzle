package it.unibo.pcd.pointerservice.verticles

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.rabbitmq.*
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.pointerservice.utils.Constants.EXCHANGE_NAME
import it.unibo.pcd.pointerservice.utils.Constants.NEW_POINTER_ADDRESS
import it.unibo.pcd.pointerservice.utils.Constants.POINTER_QUEUE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class PointerVerticle: CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger("PointerVerticle")
    private val rabbitConfig = RabbitMQOptions()
    private lateinit var client: RabbitMQClient

    override suspend fun start() {
        rabbitConfig.uri = "amqp://guest:guest@loclahost"
        client = RabbitMQClient.create(vertx, config)
        client.startAwait()

        client.queueDeclareAwait(POINTER_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueBindAwait(POINTER_QUEUE, EXCHANGE_NAME, "pointer.update")

        client.basicConsumerAwait(POINTER_QUEUE).handler {
            val res = JsonObject(it.body())
            logger.info("New pointer update from: ${res.getString("playerid")}")
            CoroutineScope(context.dispatcher()).launch {
                val response = vertx.eventBus().requestAwait<String>(NEW_POINTER_ADDRESS, res.encode())
                val payload = JsonObject().put("body", response.body())
                client.basicPublishAwait(EXCHANGE_NAME, "pointer.status", payload)
            }
        }
    }
}
