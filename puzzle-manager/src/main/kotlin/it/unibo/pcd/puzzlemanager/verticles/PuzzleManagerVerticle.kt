package it.unibo.pcd.puzzlemanager.verticles

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.rabbitmq.basicPublishAwait
import io.vertx.kotlin.rabbitmq.queueDeclareAwait
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import io.vertx.rabbitmq.RabbitMQOptions
import it.unibo.pcd.puzzlemanager.RoutesManager
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
import it.unibo.pcd.puzzlemanager.utils.JsonValidator
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class PuzzleManagerVerticle : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger("PuzzleManagerVerticle")
    private lateinit var router: Router
    private lateinit var routerManager: RoutesManager

    override suspend fun start() {

        router = Router.router(vertx)
        routerManager = RoutesManager(context, vertx = vertx)
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