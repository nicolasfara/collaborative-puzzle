package it.unibo.pcd.playerservice.verticles

import Constant.NEW_USER_QUEUE
import Constant.NEW_USER_RES_QUEUE
import io.github.cdimascio.dotenv.dotenv
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.rabbitmq.queueDeclareAwait
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.playerservice.RouterManager
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class PlayerVerticle : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(PlayerVerticle::class.java)
    private val dotenv = dotenv()
    private val rabbitConfig = RabbitMQOptions()
    private lateinit var rabbitMQClient: RabbitMQClient
    private lateinit var router: Router
    private lateinit var routerManager: RouterManager

    override suspend fun start() {
        logger.info("started")

        rabbitConfig.uri = dotenv["RABBITMQ_URI"]
        router = Router.router(vertx)
        routerManager = RouterManager(vertx)

        rabbitMQClient = RabbitMQClient.create(vertx, rabbitConfig)
        rabbitMQClient.startAwait()

        rabbitMQClient.queueDeclareAwait(NEW_USER_QUEUE, durable = true, exclusive = false, autoDelete = false)
        rabbitMQClient.queueDeclareAwait(NEW_USER_RES_QUEUE, durable = true, exclusive = false, autoDelete = false)

        router.get("/api/new_user").coroutineHandler(routerManager::createNewUser)

        vertx.createHttpServer()
                .requestHandler(router)
                .listenAwait(8081, "player-service")
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
