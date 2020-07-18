package it.unibo.pcd.puzzleservice.verticles

import io.github.cdimascio.dotenv.dotenv
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.rabbitmq.*
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.puzzleservice.Routes
import it.unibo.pcd.puzzleservice.util.Constants.EXCHANGE_NAME
import it.unibo.pcd.puzzleservice.util.Constants.POINTER_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.SWAP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class WebServerVerticle : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(WebServerVerticle::class.java)
    private val dotenv = dotenv()
    private val rabbitConfig = RabbitMQOptions()
    private lateinit var webClient: WebClient
    private lateinit var router: Router
    private lateinit var routerManager: Routes
    private lateinit var rabbitMQClient: RabbitMQClient
    private val wsPuzzleMap: MutableMap<String, MutableSet<String>> = mutableMapOf()
    private val wsPointerMap: MutableMap<String, MutableSet<String>> = mutableMapOf()

    override suspend fun start() {
        logger.info("started")

        webClient = WebClient.create(vertx)

        logger.info("Connect to ${dotenv["RABBITMQ_URI"]}")
        rabbitConfig.uri = dotenv["RABBITMQ_URI"]
        rabbitMQClient = RabbitMQClient.create(vertx, rabbitConfig)
        rabbitMQClient.startAwait()

        router = Router.router(vertx)
        routerManager = Routes(context, rabbitMQClient, webClient)

        rabbitMQClient.exchangeDeclareAwait(EXCHANGE_NAME, "topic", durable = true, autoDelete = false)
        rabbitMQClient.queueDeclareAwait(SWAP, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueDeclareAwait(POINTER_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueBindAwait(POINTER_QUEUE, EXCHANGE_NAME, "pointer.status")

        rabbitMQClient.basicConsumerAwait(POINTER_QUEUE).handler {
            val message = JsonObject(it.body())
            val puzzleid: String = message["puzzleid"]
            logger.info("Pointer update: $message")
            wsPointerMap["puzzzle.id.$puzzleid"]?.forEach { elem ->
                vertx.eventBus().publish(elem, message.encode())
            }
        }

        rabbitMQClient.basicConsumerAwait(SWAP).handler {
            val res = JsonObject(it.body())
            val puzzleid: String = res["puzzleid"]
            logger.info("New update status for pointer")
            wsPuzzleMap["puzzle.id.$puzzleid"]?.forEach { elem ->
                vertx.eventBus().publish(elem, res.encode())
            }
        }

        router.get("/").coroutineHandler(routerManager::entryPoint)
        router.post("/api/create_puzzle").coroutineHandler(routerManager::createPuzzle)
        router.post("/api/join_puzzle").coroutineHandler(routerManager::joinPuzzle)
        router.get("/api/leave_puzzle").coroutineHandler(routerManager::leavePuzzle)
        router.post("/api/swap").coroutineHandler(routerManager::swap)
        router.get("/api/score").coroutineHandler(routerManager::score)
        router.get("/api/pointer_update").coroutineHandler(routerManager::pointerUpdate)

        vertx.createHttpServer()
                .webSocketHandler {
                    CoroutineScope(context.dispatcher()).launch {
                        val wsId = it.textHandlerID()
                        if (it.path().contains("/puzzle")) {
                            val puzzleid = it.path().substringAfter("/puzzle/")
                            logger.info("New websocket connection at /puzzle: $puzzleid with id: $wsId")
                            wsPuzzleMap.putIfAbsent("puzzle.id.$puzzleid", mutableSetOf(wsId))?.add(wsId)
                        } else if (it.path().contains("/pointer")) {
                            val puzzleid = it.path().substringAfter("/pointer/")
                            logger.info("New websocket connection at /pointer: $puzzleid with id: $wsId")
                            wsPointerMap.putIfAbsent("puzzle.id.$puzzleid", mutableSetOf(wsId))?.add(wsId)
                        }
                    }
                }
                .requestHandler(router)
                .listenAwait(8080, "puzzle-service")
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
