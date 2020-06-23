package it.unibo.pcd.pointerservice.verticles

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.rabbitmq.basicConsumerAwait
import io.vertx.kotlin.rabbitmq.basicPublishAwait
import io.vertx.kotlin.rabbitmq.queueDeclareAwait
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.pointerservice.utils.Constants
import it.unibo.pcd.pointerservice.utils.Constants.POINTER_QUEUE
import it.unibo.pcd.pointerservice.utils.Constants.POINTER_UPDATE_QUEUE
import it.unibo.pcd.pointerservice.utils.Constants.PUZZLE_WITH_USER_QUEUE
import it.unibo.pcd.pointerservice.utils.Constants.PUZZLE_WITH_USER_RES_QUEUE
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
        client.queueDeclareAwait(POINTER_UPDATE_QUEUE, durable = true, exclusive = false, autoDelete = false)

        client.basicConsumerAwait(POINTER_QUEUE).handler {
            val message = JsonObject(it.body())
            logger.info("New pointer update: $message")
            launch(context.dispatcher()) {
                val request = JsonObject().put("body", message.encodePrettily())
                client.basicPublishAwait("", PUZZLE_WITH_USER_QUEUE, request)
                client.basicConsumerAwait(PUZZLE_WITH_USER_RES_QUEUE).handler { userRes ->
                    CoroutineScope(context.dispatcher()).launch {
                        val checkUserResult = JsonObject(userRes.body())
                        val res = JsonObject()
                        if (checkUserResult.getString("status") == "ok") {
                            val reply = vertx.eventBus().requestAwait<String>(Constants.NEW_POINTER_ADDRESS, message.encodePrettily())
                            res.put("body", reply.body())
                        } else {
                            logger.warn("Problem with puzzle or playerid into the request")
                            res.put("body", checkUserResult.encodePrettily())
                        }
                        logger.info("Send res: $res")
                        client.basicPublishAwait("", POINTER_UPDATE_QUEUE, res)
                    }
                }
            }
        }
    }
}