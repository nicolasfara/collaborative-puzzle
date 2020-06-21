package it.unibo.pcd.puzzleservice.verticles

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.viartemev.thewhiterabbit.channel.channel
import com.viartemev.thewhiterabbit.queue.QueueSpecification
import com.viartemev.thewhiterabbit.queue.declareQueue
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.pcd.puzzleservice.Routes
import it.unibo.pcd.puzzleservice.util.Constants.JOIN_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.JOIN_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.LEAVE_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.LEAVE_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.LOGGING_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_USER_QUEUE
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

class WebserverVerticle : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(WebserverVerticle::class.java)
    private lateinit var router: Router
    private lateinit var routerManager: Routes

    private val connection: Connection = ConnectionFactory().newConnection()

    override suspend fun start() {
        logger.info("${WebserverVerticle::class.java} started")

        router = Router.router(vertx)
        routerManager = Routes(context, connection)

        connection.channel {
            declareQueue(QueueSpecification(NEW_PUZZLE_QUEUE, durable = true))
            declareQueue(QueueSpecification(NEW_PUZZLE_RES_QUEUE, durable = true))
            declareQueue(QueueSpecification(JOIN_PUZZLE_QUEUE, durable = true))
            declareQueue(QueueSpecification(JOIN_PUZZLE_RES_QUEUE, durable = true))
            declareQueue(QueueSpecification(LEAVE_PUZZLE_QUEUE, durable = true))
            declareQueue(QueueSpecification(LEAVE_PUZZLE_RES_QUEUE, durable = true))
            declareQueue(QueueSpecification(SWAP_PUZZLE_QUEUE, durable = true))
            declareQueue(QueueSpecification(SWAP_PUZZLE_RES_QUEUE, durable = true))
            declareQueue(QueueSpecification(UPDATE_PUZZLE_QUEUE, durable = true))
            declareQueue(QueueSpecification(NEW_USER_QUEUE, durable = true))
            declareQueue(QueueSpecification(NEW_USER_RES_QUEUE, durable = true))
            declareQueue(QueueSpecification(SCORE_QUEUE, durable = true))
            declareQueue(QueueSpecification(POSITION_POINTER_QUEUE, durable = true))
            declareQueue(QueueSpecification(UPDATE_POINTER_QUEUE, durable = true))
            declareQueue(QueueSpecification(LOGGING_QUEUE, durable = true))
        }

        router.get("/").coroutineHandler(routerManager::entryPoint)
        router.get("/api/create_puzzle").coroutineHandler(routerManager::createPuzzle)
        router.get("/api/join_puzzle").coroutineHandler(routerManager::joinPuzzle)
        router.get("/api/leave_puzzle").coroutineHandler(routerManager::leavePuzzle)
        router.get("/api/swap").coroutineHandler(routerManager::swap)
        router.get("/api/score").coroutineHandler(routerManager::score)

        vertx.createHttpServer()
                .requestHandler(router)
                .listenAwait(8080)

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
