package it.unibo.pcd.puzzlemanager.verticles

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.pcd.puzzlemanager.RoutesManager
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