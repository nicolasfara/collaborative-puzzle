package it.unibo.pcd.puzzlemanager.verticles

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.rabbitmq.basicConsumerAwait
import io.vertx.kotlin.rabbitmq.basicPublishAwait
import io.vertx.kotlin.rabbitmq.queueDeclareAwait
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.puzzlemanager.utils.Constants.POINTER_CHECK
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_JOIN_ADDRESS
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_JOIN_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_JOIN_RES_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_LEAVE_ADDRESS
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_LEAVE_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_LEAVE_RES_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_NEW_ADDRESS
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_NEW_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_NEW_RES_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_SWAP_ADDRESS
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_SWAP_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_SWAP_RES_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_WITH_USER_QUEUE
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_WITH_USER_RES_QUEUE
import it.unibo.pcd.puzzlemanager.utils.JsonValidator
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
        client.queueDeclareAwait(PUZZLE_WITH_USER_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(PUZZLE_WITH_USER_RES_QUEUE, durable = true, exclusive = false, autoDelete = false)

        client.basicConsumerAwait(PUZZLE_NEW_QUEUE).handler {
            val request = JsonObject(it.body())
            logger.info("New puzzle request: $request")
            if (validator.validateNewPuzzle(request)) {
                logger.info("Valid")
                launch {
                    val reply = vertx.eventBus().requestAwait<String>(PUZZLE_NEW_ADDRESS, request.encodePrettily())
                    logger.info("Communicate back puzzleid")
                    resultOn(PUZZLE_NEW_RES_QUEUE, JsonObject(reply.body()))
                }
            } else {
                logger.warn("Malformed input: $request")
                val payloadError = JsonObject().put("status", "malformed input")
                val errorResult = JsonObject().put("body", payloadError.encodePrettily())
                launch { resultOn(PUZZLE_NEW_RES_QUEUE, errorResult) }
            }
        }

        client.basicConsumerAwait(PUZZLE_JOIN_QUEUE).handler {
            val request = JsonObject(it.body())
            logger.info("Join puzzle request")
            if (validator.validateJoinPuzzle(request)) {
                logger.info("Join puzzle request valid")
                launch {
                    val reply = vertx.eventBus().requestAwait<String>(PUZZLE_JOIN_ADDRESS, request.encodePrettily())
                    logger.info("Join response: ${reply.body()}")
                    resultOn(PUZZLE_JOIN_RES_QUEUE, JsonObject(reply.body()))
                }
            } else {
                logger.warn("Join puzzle request malformed: $request")
                val payloadError = JsonObject().put("status", "malformed input")
                val payload = JsonObject().put("body", payloadError.encodePrettily())
                launch { resultOn(PUZZLE_JOIN_RES_QUEUE, payload) }
            }
        }

        client.basicConsumerAwait(PUZZLE_LEAVE_QUEUE).handler {
            val request = JsonObject(it.body())
            logger.info("Leave puzzle request")
            if (validator.validateLeavePuzzle(request)) {
                logger.info("Leave puzzle request valid")
                launch {
                    val reply = vertx.eventBus().requestAwait<String>(PUZZLE_LEAVE_ADDRESS, request.encodePrettily())
                    logger.info("Leave response: ${reply.body()}")
                    resultOn(PUZZLE_LEAVE_RES_QUEUE, JsonObject(reply.body()))
                }
            } else {
                logger.warn("Leave puzzle request malformed: $request")
                val payloadError = JsonObject().put("status", "malformed input")
                val payload = JsonObject().put("body", payloadError.encodePrettily())
                launch { resultOn(PUZZLE_LEAVE_RES_QUEUE, payload) }
            }
        }

        client.basicConsumerAwait(PUZZLE_SWAP_QUEUE).handler {
            val request = JsonObject(it.body())
            logger.info("Swap request")
            if (validator.validateSwapPuzzle(request)) {
                logger.info("Swap puzzle request valid")
                launch {
                    val reply = vertx.eventBus().requestAwait<String>(PUZZLE_SWAP_ADDRESS, request.encodePrettily())
                    logger.info("Swap response: ${reply.body()}")
                    resultOn(PUZZLE_SWAP_RES_QUEUE, JsonObject(reply.body()))
                }
            } else {
                logger.warn("Swap puzzle request malformed")
                val payloadError = JsonObject().put("status", "malformed input")
                val payload = JsonObject().put("body", payloadError.encodePrettily())
                launch { resultOn(PUZZLE_SWAP_RES_QUEUE, payload) }
            }
        }

        client.basicConsumerAwait(PUZZLE_WITH_USER_QUEUE).handler {
            val request = JsonObject(it.body())
            logger.info("Check use in puzzle request")
            if (validator.validateUserInPuzzle(request)) {
                launch {
                    val res = vertx.eventBus().requestAwait<String>(POINTER_CHECK, request.encodePrettily())
                    val payload = JsonObject().put("body", res.body())
                    resultOn(PUZZLE_WITH_USER_RES_QUEUE, payload)
                }
            } else {
                logger.warn("User in puzzle request malformed")
                val payloadError = JsonObject().put("status", "malformed input")
                val payload = JsonObject().put("body", payloadError.encodePrettily())
                launch { resultOn(PUZZLE_WITH_USER_RES_QUEUE, payload) }
            }
        }
    }

    private suspend fun resultOn(queue: String, message: JsonObject) {
        client.basicPublishAwait("", queue, message)
    }
}