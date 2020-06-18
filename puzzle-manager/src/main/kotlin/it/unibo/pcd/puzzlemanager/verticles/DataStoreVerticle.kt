package it.unibo.pcd.puzzlemanager.verticles

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.mongo.insertAwait
import it.unibo.pcd.puzzlemanager.Constants
import it.unibo.pcd.puzzlemanager.PuzzleDbManager
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class DataStoreVerticle : CoroutineVerticle() {
    private lateinit var mongoClient: MongoClient
    private lateinit var dbManager: PuzzleDbManager
    private val logger = LoggerFactory.getLogger("DataStoreVerticle")

    override suspend fun start() {
        val mongoConfig = JsonObject().put("db_name", "puzzle_manager")
                .put("connection_string", "mongodb://localhost:27017")
        mongoClient = MongoClient.create(vertx, mongoConfig)
        dbManager = PuzzleDbManager(mongoClient)

        vertx.eventBus().localConsumer<String>(Constants.PUZZLE_NEW_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("New message for new puzzle: $message")
            launch {
                val puzzleId = dbManager.createNewPuzzle(message)
                logger.info("Add new puzzle to DB with ID: $puzzleId")
                val responseBody = JsonObject()
                        .put("player-id", message.getValue("player-id"))
                        .put("puzzle-id", puzzleId)

                val response = JsonObject().put("body", responseBody.encodePrettily())
                it.reply(response.encodePrettily())
            }
        }
    }
}