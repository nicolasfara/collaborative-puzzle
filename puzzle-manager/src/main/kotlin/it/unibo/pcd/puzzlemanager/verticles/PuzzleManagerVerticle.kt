package it.unibo.pcd.puzzlemanager.verticles

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
import it.unibo.pcd.puzzlemanager.RoutesManager
import it.unibo.pcd.puzzlemanager.utils.Constants
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class PuzzleManagerVerticle : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger("PuzzleManagerVerticle")
    private lateinit var router: Router
    private lateinit var routerManager: RoutesManager
    private lateinit var rabbitMQClient: RabbitMQClient
    private var rabbitConfig = RabbitMQOptions()

    override suspend fun start() {

        rabbitConfig.uri = "amqp://guest:guest@localhost"
        rabbitMQClient = RabbitMQClient.create(vertx, config)
        rabbitMQClient.startAwait()
        rabbitMQClient.queueDeclareAwait(Constants.SWAP, durable = true,exclusive = false, autoDelete = false)
        router = Router.router(vertx)
        routerManager = RoutesManager(context, vertx = vertx, rabbitMQClient = rabbitMQClient)
        router.post("/api/new_puzzle").coroutineHandler(routerManager::newPuzzle)
        router.post("/api/join_puzzle").coroutineHandler(routerManager::joinPuzzle)
        router.post("/api/leave_puzzle").coroutineHandler(routerManager::leavePuzzle)
        router.post("/api/swap").coroutineHandler(routerManager::swap)

        vertx.createHttpServer()
                .requestHandler(router)
                .listenAwait(8082)

    }
    private fun Route.coroutineHandler(fn:suspend (RoutingContext)->Unit){
        handler{
            launch(vertx.dispatcher()){
                try{
                    fn(it)
                }catch (ex:Exception){
                    it.fail(ex)
                }
            }
        }
    }
}