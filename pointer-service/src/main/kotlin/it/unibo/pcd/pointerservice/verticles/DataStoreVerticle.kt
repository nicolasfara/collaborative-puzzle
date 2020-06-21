package it.unibo.pcd.pointerservice.verticles

import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.UpdateOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.mongo.findAwait
import io.vertx.kotlin.ext.mongo.updateCollectionWithOptionsAwait
import io.vertx.kotlin.ext.web.handler.graphql.apolloWSOptionsOf
import it.unibo.pcd.pointerservice.utils.Constants
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class DataStoreVerticle: CoroutineVerticle() {
    private lateinit var mongoClient: MongoClient
    private val logger = LoggerFactory.getLogger("DataStoreVerticle")

    override suspend fun start() {
        val mongoConfig = JsonObject().put("db_name", "pointer_service")
                .put("connection_string", "mongodb://localhost:27017")
        mongoClient = MongoClient.create(vertx, mongoConfig)

        vertx.eventBus().localConsumer<String>(Constants.NEW_POINTER_ADDRESS).handler {
            val message = JsonObject(it.body())
            val query = message
            query.remove("pointer")
            val update = JsonObject().put("\$set", JsonObject().put("pointer", message.getString("pointer")))
            val option = UpdateOptions().setUpsert(true)
            launch {
                val res = mongoClient.updateCollectionWithOptionsAwait("pointer", query, update, option)
                if (res!!.docMatched > 0) {
                    logger.info("Update successfully new pointer position")
                    val playerList = getPlayerPosition(message.getString("puzzle-id"))
                    it.reply(JsonObject().put("position", playerList))
                } else {
                    logger.warn("Error on insert new position into DB")
                }
            }
        }
    }

    private suspend fun getPlayerPosition(puzzleId: String): List<JsonObject> {
        val query = JsonObject().put("puzzle-id", puzzleId)
        val res = mongoClient.findAwait("pointer", query)
        return res.map { it.remove("puzzle-id") as JsonObject }
    }
}