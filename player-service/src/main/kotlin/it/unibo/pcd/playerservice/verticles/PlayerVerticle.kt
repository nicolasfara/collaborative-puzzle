package it.unibo.pcd.playerservice.verticles


import Constant.NEW_PLAYER
import Constant.NEW_USER_QUEUE
import Constant.NEW_USER_RES_QUEUE
import io.github.serpro69.kfaker.Faker
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.rabbitmq.basicConsumerAwait
import io.vertx.kotlin.rabbitmq.basicPublishAwait
import io.vertx.kotlin.rabbitmq.queueDeclareAwait
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class PlayerVerticle : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(PlayerVerticle::class.java)
    private lateinit var client: RabbitMQClient
    private val faker = Faker()

    override suspend fun start() {
        logger.info("${PlayerVerticle::class.java} started")
        client = RabbitMQClient.create(vertx, config)
        client.startAwait()

        client.queueDeclareAwait(NEW_USER_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(NEW_USER_RES_QUEUE, durable = true, exclusive = false, autoDelete = false)

        client.basicConsumerAwait(NEW_USER_QUEUE).handler {
            logger.info("Receive message from Puzzle Service")

            val user = JsonObject().put("playername", generateFakeName())
            launch {
                val reply = awaitResult<Message<String>> { handler ->
                    vertx.eventBus().request(NEW_PLAYER, user.encodePrettily(), handler)
                }
                logger.info("Publish playername: ${reply.body()}")
                client.basicPublishAwait("", NEW_USER_RES_QUEUE, JsonObject(reply.body()))
            }
        }
    }

    /**
     * Generate name based of City in the world.
     */
    private fun generateFakeName(): String {
        return faker.address.city().replace("\\s".toRegex(), "")
    }

}
