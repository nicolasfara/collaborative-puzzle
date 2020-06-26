package it.unibo.pcd.client.verticles

import io.vertx.core.http.HttpClient
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient

import io.vertx.kotlin.coroutines.CoroutineVerticle
import it.unibo.pcd.client.ui.PuzzleBoard
import it.unibo.pcd.client.utils.Constants.SWAP_ADDRESS
import it.unibo.pcd.client.utils.Constants.SWAP_URI

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NetManagerVerticle(private val puzzleBoard: PuzzleBoard) : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(NetManagerVerticle::class.java)
    private lateinit var webClient: WebClient
    private lateinit var wsClient: HttpClient

    override suspend fun start() {
        logger.info("Verticle deployed")
        var puzzleid = ""
        webClient = WebClient.create(vertx)
        wsClient = vertx.createHttpClient()

        vertx.eventBus().localConsumer<String>(SWAP_ADDRESS).handler {
            val swapMessage = JsonObject(it.body())
            puzzleid = swapMessage.getString("puzzleid")
            logger.info("New swap request")
            webClient.post(8080, "localhost", SWAP_URI).sendJson(swapMessage) {
                logger.info("Response to swap")
            }
            puzzleBoard.paintPuzzle()
        }

        /* This generate error Connection refused: localhost/127.0.0.1:8080 */
        /*wsClient.webSocketAwait(8080, "localhost", PUZZLE_WS_URI + puzzleid).handler {
            val newPuzzleState = JsonObject(String(it.bytes))
            logger.info("New puzzle state: $newPuzzleState")
            CoroutineScope(context.dispatcher()).launch {
                vertx.eventBus().publish(UPDATE_STATE_ADDRESS, newPuzzleState.encode())
            }
        }

        wsClient.webSocketAwait(8080, "localhost", POINTER_WS_URI + puzzleid).handler {
            val newPointerState = JsonObject(String(it.bytes))
            logger.info("New pointer update: $newPointerState")
            CoroutineScope(context.dispatcher()).launch {
                vertx.eventBus().publish(POINTER_ADDRESS, newPointerState.encode())
            }
        }*/
    }

    override suspend fun stop() {
        logger.info("Shutdown verticle")
    }
}