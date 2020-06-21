package it.unibo.pcd.pointerservice.verticles

import com.sun.org.apache.bcel.internal.Const
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.core.json.JsonArray
import io.vertx.kotlin.coroutines.CoroutineVerticle
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
            launch {
                client.basicPublishAwait("", PUZZLE_WITH_USER_QUEUE, message)
                client.basicConsumerAwait(PUZZLE_WITH_USER_RES_QUEUE).handler { userRes ->
                    val checkUserResult = JsonObject(userRes.body())
                    val res = JsonObject()
                    if (checkUserResult.getString("status") == "ok") {
                        launch {
                            val reply = vertx.eventBus().requestAwait<String>(Constants.NEW_POINTER_ADDRESS, message.encodePrettily())
                            res.put("body", reply.body())
                        }
                    } else {
                       res.put("body", checkUserResult.encodePrettily())
                    }
                    launch { client.basicPublishAwait("", POINTER_UPDATE_QUEUE, res) }
                }
            }
        }
    }
}