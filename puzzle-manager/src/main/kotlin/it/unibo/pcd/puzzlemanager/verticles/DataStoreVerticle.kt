package it.unibo.pcd.puzzlemanager.verticles

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import it.unibo.pcd.puzzlemanager.utils.Constants
import it.unibo.pcd.puzzlemanager.db.PuzzleDbManager
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class DataStoreVerticle : CoroutineVerticle() {
    private lateinit var mongoClient: MongoClient
    private lateinit var dbManager: PuzzleDbManager
    private val dotenv: Dotenv = dotenv()
    private val logger = LoggerFactory.getLogger("DataStoreVerticle")

    override suspend fun start() {
        val mongoConfig = JsonObject().put("db_name", "puzzle_manager")
                .put("connection_string", dotenv["MONGODB_URI"])
        mongoClient = MongoClient.create(vertx, mongoConfig)
        dbManager = PuzzleDbManager(mongoClient)

        vertx.eventBus().localConsumer<String>(Constants.PUZZLE_NEW_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("New message for new puzzle: $message")
            launch {
                val response = dbManager.createNewPuzzle(message)
                logger.info("Add new puzzle to DB with ID: $response")
                val responseBody = response
                        .put("playerid", message.getValue("playerid"))
                it.reply(responseBody.encode())
            }
        }

        vertx.eventBus().localConsumer<String>(Constants.PUZZLE_JOIN_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("Join puzzle request: $message")
            launch {
                val find = JsonObject().put("_id", message.getString("puzzleid"))
                dbManager.joinPuzzle(find, message.getString("playerid")).ifPresentOrElse({ puzzleProp ->
                    logger.info("Puzzle found")
                    it.reply(puzzleProp.encode())
                }, {
                    logger.warn("No puzzle with given id: $find")
                    val errorRes = JsonObject().put("status", "No puzzle exist with the given id")
                    it.reply(errorRes.encode())
                })
            }
        }

        vertx.eventBus().localConsumer<String>(Constants.PUZZLE_LEAVE_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("Leave puzzle request: $message")
            launch {
                val find = JsonObject().put("_id", message.getString("puzzleid"))
                dbManager.leavePuzzle(find, message.getString("playerid")).ifPresentOrElse({ puzzleProp ->
                    it.reply(puzzleProp.encode())
                }, {
                    logger.warn("Puzzle id or player not found")
                    val errorMsg = JsonObject().put("status", "Puzzle id not found or player not found")
                    it.reply(errorMsg.encode())
                })
            }
        }

        vertx.eventBus().localConsumer<String>(Constants.PUZZLE_SWAP_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("Swap puzzle request: $message")
            launch {
                val source = message.getString("source").toInt()
                val destination = message.getString("destination").toInt()
                val find = JsonObject().put("_id", message.getString("puzzleid"))
                dbManager.swapPuzzle(find, source, destination).ifPresentOrElse({ puzzleProp ->
                    it.reply(puzzleProp.encode())
                }, {
                    logger.warn("Puzzle id or player not found")
                    val errorMsg = JsonObject().put("status", "Puzzle id not found or player not found")
                    it.reply(errorMsg.encode())
                })
            }
        }

        vertx.eventBus().localConsumer<String>(Constants.POINTER_CHECK).handler {
            val message = JsonObject(it.body())
            launch {
                val isInPuzzle = dbManager.isUserInPuzzle(message.getString("playerid"), message.getString("puzzleid"))
                logger.info("Find: $isInPuzzle")
                if (isInPuzzle) {
                    val res = JsonObject().put("status", "ok")
                    it.reply(res.encodePrettily())
                } else {
                    val res = JsonObject().put("status", "No puzzle or player found")
                    it.reply(res.encodePrettily())
                }
            }
        }
    }
}