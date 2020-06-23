package it.unibo.pcd.puzzleservice.verticles

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.rabbitmq.*
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.puzzleservice.Routes
import it.unibo.pcd.puzzleservice.util.Constants.EXCHANGE_NAME
import it.unibo.pcd.puzzleservice.util.Constants.POINTER_QUEUE
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

        rabbitMQClient.queueDeclareAwait(POINTER_QUEUE, durable = true, autoDelete = false, exclusive = false)
        rabbitMQClient.queueBindAwait(POINTER_QUEUE, EXCHANGE_NAME, "pointer.status")

        rabbitMQClient.basicConsumerAwait(POINTER_QUEUE).handler {
            logger.info("New update status for pointer")
        }

        router.get("/").coroutineHandler(routerManager::entryPoint)
        router.get("/api/create_puzzle").coroutineHandler(routerManager::createPuzzle)
        router.get("/api/join_puzzle").coroutineHandler(routerManager::joinPuzzle)
        router.get("/api/leave_puzzle").coroutineHandler(routerManager::leavePuzzle)
        router.get("/api/swap").coroutineHandler(routerManager::swap)
        router.get("/api/score").coroutineHandler(routerManager::score)
        router.get("/api/pointer_update").coroutineHandler(routerManager::pointerUpdate)

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
