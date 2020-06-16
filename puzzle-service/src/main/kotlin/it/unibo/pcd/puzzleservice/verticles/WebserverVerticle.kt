package it.unibo.pcd.puzzleservice.verticles

import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.puzzleservice.Routes
import org.slf4j.LoggerFactory

class WebserverVerticle : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger(WebserverVerticle::class.java)
    private lateinit var rabbitMQClient: RabbitMQClient
    private lateinit var server: HttpServer
    private lateinit var router: Router
    private lateinit var routerManager: Routes

    override suspend fun start() {
        logger.info("${WebserverVerticle::class.java} started")
        val config = RabbitMQOptions()
        config.uri = "amqp://guest:guest@localhost"
        rabbitMQClient = RabbitMQClient.create(vertx, config)
        try {
            rabbitMQClient.startAwait()
        } catch (ex: Exception) {
            logger.error("Error on connection", ex.cause)
        }

        router = Router.router(vertx)
        routerManager = Routes(context,rabbitMQClient)

        router.get("/").handler(routerManager::entryPoint)
        router.get("/api/create_puzzle").handler(routerManager::createPuzzle)
        router.get("/api/join_puzzle").handler(routerManager::joinPuzzle)
        router.get("/api/leave_puzzle").handler(routerManager::leavePuzzle)
        router.get("/api/move").handler(routerManager::move)
        router.get("/api/score").handler(routerManager::score)

        vertx.createHttpServer()
                .requestHandler(router)
                .listenAwait(8080)
    }
}
