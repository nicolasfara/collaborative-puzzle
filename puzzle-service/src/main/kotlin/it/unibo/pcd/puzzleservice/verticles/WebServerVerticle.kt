package it.unibo.pcd.puzzleservice.verticles

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.rabbitmq.queueDeclareAwait
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.puzzleservice.Routes
import it.unibo.pcd.puzzleservice.util.Constants.JOIN_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.JOIN_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.LEAVE_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.LEAVE_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.LOGGING_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_USER_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.POSITION_POINTER_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.SCORE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.SWAP_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.SWAP_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.UPDATE_POINTER_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.UPDATE_PUZZLE_QUEUE
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WebServerVerticle : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(WebServerVerticle::class.java)
    private val rabbitConfig = RabbitMQOptions()
    private lateinit var webClient: WebClient
    private lateinit var router: Router
    private lateinit var routerManager: Routes
    private lateinit var rabbitMQClient: RabbitMQClient

    override suspend fun start() {
        logger.info("started")

        webClient = WebClient.create(vertx)

        rabbitConfig.uri = "amqp://guest:guest@localhost"
        rabbitMQClient = RabbitMQClient.create(vertx, config)
        rabbitMQClient.startAwait()

        router = Router.router(vertx)
        routerManager = Routes(context, rabbitMQClient, webClient)

        rabbitMQClient.queueDeclareAwait(NEW_PUZZLE_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(NEW_PUZZLE_RES_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(JOIN_PUZZLE_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(JOIN_PUZZLE_RES_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(LEAVE_PUZZLE_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(LEAVE_PUZZLE_RES_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(SWAP_PUZZLE_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(SWAP_PUZZLE_RES_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(UPDATE_PUZZLE_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(NEW_USER_RES_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(SCORE_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(POSITION_POINTER_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(UPDATE_POINTER_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(LOGGING_QUEUE, durable = true, autoDelete = false, exclusive = false)

        router.get("/").coroutineHandler(routerManager::entryPoint)
        router.get("/api/create_puzzle").coroutineHandler(routerManager::createPuzzle)
        router.get("/api/join_puzzle").coroutineHandler(routerManager::joinPuzzle)
        router.get("/api/leave_puzzle").coroutineHandler(routerManager::leavePuzzle)
        router.get("/api/swap").coroutineHandler(routerManager::swap)
        router.get("/api/score").coroutineHandler(routerManager::score)

        vertx.createHttpServer()
                .requestHandler(router)
                .listenAwait(8080, "localhost")
    }

    private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler {
            launch(vertx.dispatcher()) {
                try {
                    fn(it)
                } catch (ex: Exception) {
                    it.fail(ex)
                }
            }
        }
    }
}
