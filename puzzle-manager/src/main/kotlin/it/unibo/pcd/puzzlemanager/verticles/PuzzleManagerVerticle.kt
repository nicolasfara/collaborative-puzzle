package it.unibo.pcd.puzzlemanager.verticles

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.rabbitmq.basicConsumerAwait
import io.vertx.kotlin.rabbitmq.basicPublishAwait
import io.vertx.kotlin.rabbitmq.queueDeclareAwait
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_JOIN_QUEUE
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_JOIN_RES_QUEUE
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_LEAVE_QUEUE
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_LEAVE_RES_QUEUE
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_NEW_ADDRESS
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_NEW_QUEUE
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_NEW_RES_QUEUE
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_SWAP_QUEUE
import it.unibo.pcd.puzzlemanager.Constants.PUZZLE_SWAP_RES_QUEUE
import it.unibo.pcd.puzzlemanager.JsonValidator
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class PuzzleManagerVerticle : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger("PuzzleManagerVerticle")
    private val rabbitConfig = RabbitMQOptions()
    private val validator = JsonValidator()
    private lateinit var client: RabbitMQClient

    override suspend fun start() {
        rabbitConfig.uri = "amqp://guest:guest@localhost"
        client = RabbitMQClient.create(vertx, config)
        client.startAwait()

        client.queueDeclareAwait(PUZZLE_JOIN_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(PUZZLE_JOIN_RES_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(PUZZLE_LEAVE_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(PUZZLE_LEAVE_RES_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(PUZZLE_NEW_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(PUZZLE_NEW_RES_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(PUZZLE_SWAP_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(PUZZLE_SWAP_RES_QUEUE, durable = true, exclusive = false, autoDelete = false)

        client.basicConsumerAwait(PUZZLE_NEW_QUEUE).handler {
            val request = JsonObject(it.body())
            logger.info("New puzzle request: $request")
            if (validator.validateNewPuzzle(request)) {
                logger.info("Valid")
                launch {
                    val reply = awaitResult<Message<String>> { handler ->
                        vertx.eventBus().send(PUZZLE_NEW_ADDRESS, request.encodePrettily(), handler)
                    }
                    logger.info("Communicate back puzzle-id")
                    resultOnNewPuzzle(JsonObject(reply.body()))
                }
            } else {
                logger.warn("Malformed input: $request")
                val payloadError = JsonObject().put("status", "malformed input")
                val errorResult = JsonObject().put("body", payloadError.encodePrettily())
                launch { resultOnNewPuzzle(errorResult) }
            }
        }

        client.basicConsumerAwait(PUZZLE_JOIN_QUEUE).handler {
            logger.info("Join puzzle request")
        }

        client.basicConsumerAwait(PUZZLE_LEAVE_QUEUE).handler {
            logger.info("Leave puzzle request")
        }

        client.basicConsumerAwait(PUZZLE_SWAP_QUEUE).handler {
            logger.info("Swap request")
        }
    }

    private suspend fun resultOnNewPuzzle(message: JsonObject) {
        client.basicPublishAwait("", PUZZLE_NEW_RES_QUEUE, message)
    }
}