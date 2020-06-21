package it.unibo.pcd.playerservice.verticles

import Constant.NEW_PLAYER
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.ext.mongo.insertAwait
import kotlinx.coroutines.launch

class DataPlayerVerticle : CoroutineVerticle() {
    private lateinit var mongoClient: MongoClient
    private val logger = LoggerFactory.getLogger("DataPlayerVerticle")

    override suspend fun start() {
        val mongoConfig = JsonObject().put("db_name", "Player")
                .put("connection_string", "mongodb://localhost:27017")
        mongoClient = MongoClient.create(vertx, mongoConfig)

        vertx.eventBus().localConsumer<String>(NEW_PLAYER).handler {
            val message = JsonObject(it.body())
            logger.info("New message for new player: $message")
            launch {
                mongoClient.insertAwait("player", message)
                val responseBody = JsonObject()
                        .put("playerid", message.getValue("playername"))

                val playerMessage = JsonObject().put("body", responseBody.encodePrettily())
                it.reply(playerMessage.encodePrettily())
            }
        }
    }
}
