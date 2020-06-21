package it.unibo.pcd.puzzlemanager.verticles

import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.mongo.findAwait
import it.unibo.pcd.puzzlemanager.utils.Constants
import it.unibo.pcd.puzzlemanager.db.PuzzleDbManager
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.math.log

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

        vertx.eventBus().localConsumer<String>(Constants.PUZZLE_JOIN_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("Join puzzle request: $message")
            launch {
                val find = JsonObject().put("_id", message.getString("puzzle-id"))
                dbManager.joinPuzzle(find, message.getString("player-id")).ifPresentOrElse({ puzzleProp ->
                    logger.info("Puzzle found")
                    val res = JsonObject().put("body", puzzleProp.encodePrettily())
                    it.reply(res.encodePrettily())
                }, {
                    logger.warn("No puzzle with given id: $find")
                    val errorRes = JsonObject().put("status", "No puzzle exist with the given id")
                    val res = JsonObject().put("body", errorRes.encodePrettily())
                    it.reply(res.encodePrettily())
                })
            }
        }

        vertx.eventBus().localConsumer<String>(Constants.PUZZLE_LEAVE_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("Leave puzzle request: $message")
            launch {
                val find = JsonObject().put("_id", message.getString("puzzle-id"))
                dbManager.leavePuzzle(find, message.getString("player-id")).ifPresentOrElse({ puzzleProp ->
                    val res = JsonObject().put("body", puzzleProp.encodePrettily())
                    it.reply(res.encodePrettily())
                }, {
                    logger.warn("Puzzle id or player not found")
                    val errorMsg = JsonObject().put("status", "Puzzle id not found or player not found")
                    val res = JsonObject().put("body", errorMsg.encodePrettily())
                    it.reply(res.encodePrettily())
                })
            }
        }

        vertx.eventBus().localConsumer<String>(Constants.PUZZLE_SWAP_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("Swap puzzle request: $message")
            launch {
                val source = message.getString("source").toInt()
                val destination = message.getString("destination").toInt()
                val find = JsonObject().put("_id", message.getString("puzzle-id"))
                dbManager.swapPuzzle(find, source, destination).ifPresentOrElse({ puzzleProp ->
                    val res = JsonObject().put("body", puzzleProp.encodePrettily())
                    it.reply(res.encodePrettily())
                }, {
                    logger.warn("Puzzle id or player not found")
                    val errorMsg = JsonObject().put("status", "Puzzle id not found or player not found")
                    val res = JsonObject().put("body", errorMsg.encodePrettily())
                    it.reply(res.encodePrettily())
                })
            }
        }
    }
}