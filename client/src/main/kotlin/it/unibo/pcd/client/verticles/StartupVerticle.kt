package it.unibo.pcd.client.verticles

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendJsonAwait
import it.unibo.pcd.client.ui.InitialFrame
import it.unibo.pcd.client.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StartupVerticle(private val view: InitialFrame) : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(StartupVerticle::class.java)
    private lateinit var webClient: WebClient

    override suspend fun start() {
        logger.info("Verticle deployed")

        webClient = WebClient.create(vertx)

        vertx.eventBus().localConsumer<String>(Constants.CREATE_ADDRESS).handler { message ->
            val newPuzzleMessage = JsonObject(message.body())
            CoroutineScope(context.dispatcher()).launch {
                val result = webClient.post(8080, "localhost", Constants.CREATE_URI)
                        .sendJsonAwait(newPuzzleMessage)
                view.createPuzzleBoard(result = result.bodyAsJsonObject())
            }
        }

        vertx.eventBus().localConsumer<String>(Constants.JOIN_ADDRESS).handler { message ->
            val joinPuzzleMessage = JsonObject(message.body())
            CoroutineScope(context.dispatcher()).launch {
                val result = webClient.post(8080, "localhost", Constants.JOIN_URI)
                        .sendJsonAwait(joinPuzzleMessage)
                view.joinPuzzleBoard(result = result.bodyAsJsonObject())
            }
        }
    }
}