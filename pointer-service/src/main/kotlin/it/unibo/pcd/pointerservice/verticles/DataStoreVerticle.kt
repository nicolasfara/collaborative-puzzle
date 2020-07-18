package it.unibo.pcd.pointerservice.verticles

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.UpdateOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.mongo.findAwait
import io.vertx.kotlin.ext.mongo.updateCollectionWithOptionsAwait
import it.unibo.pcd.pointerservice.utils.Constants
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class DataStoreVerticle: CoroutineVerticle() {
    private lateinit var mongoClient: MongoClient
    private val logger = LoggerFactory.getLogger("DataStoreVerticle")
    private val dotenv: Dotenv = dotenv()

    override suspend fun start() {
        val mongoConfig = JsonObject().put("db_name", "pointer_service")
                .put("connection_string", dotenv["MONGODB_URI"])
        mongoClient = MongoClient.create(vertx, mongoConfig)

        vertx.eventBus().localConsumer<String>(Constants.NEW_POINTER_ADDRESS).handler {
            val message = JsonObject(it.body())
            logger.info("Pointer message: $message")
            val query = message.copy()
            query.remove("pointer")
            val pos = message.getString("pointer")
            val update = JsonObject().put("\$set", JsonObject().put("pointer", pos))
            val option = UpdateOptions().setUpsert(true)
            launch {
                val res = mongoClient.updateCollectionWithOptionsAwait("pointer", query, update, option)
                if (res!!.docModified > 0) {
                    logger.info("Update successfully new pointer position")
                    val playerList = getPlayerPosition(message.getString("puzzleid"))
                    it.reply(JsonObject().put("position", playerList).encodePrettily())
                } else {
                    logger.warn("Error on insert new position into DB")
                    val rep = JsonObject().put("status", "error on insert into DB")
                    it.reply(rep.encodePrettily())
                }
            }
        }
    }

    private suspend fun getPlayerPosition(puzzleId: String): List<JsonObject> {
        val query = JsonObject().put("puzzleid", puzzleId)
        val res = mongoClient.findAwait("pointer", query)
        return res.onEach { it.remove("puzzleid") }
    }
}