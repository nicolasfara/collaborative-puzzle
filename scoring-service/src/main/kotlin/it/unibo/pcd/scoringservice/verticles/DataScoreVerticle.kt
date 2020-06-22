package it.unibo.pcd.scoringservice.verticles

import Constant.NEW_SCORE
import Constant.STATISTIC_USER
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.MongoClientUpdateResult
import io.vertx.ext.mongo.UpdateOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.mongo.findAwait
import kotlinx.coroutines.launch


class DataScoreVerticle : CoroutineVerticle() {
    private lateinit var mongoClient: MongoClient
    private val logger = LoggerFactory.getLogger("DataScoreVerticle")

    override suspend fun start() {
        val mongoConfig = JsonObject().put("db_name", "Score")
                .put("connection_string", "mongodb://localhost:27017")
        mongoClient = MongoClient.create(vertx, mongoConfig)

        vertx.eventBus().localConsumer<String>(NEW_SCORE).handler {
            val message = JsonObject(it.body())
            logger.info("New message for new score: $message")
            val query = JsonObject()
                    .put("puzzleid", message.getValue("puzzleid"))
                    .put("playerid", message.getValue("playerid"))
            val update = JsonObject().put("\$inc", JsonObject()
                    .put("score", message.getValue("score")))

            launch {
                val options: UpdateOptions = UpdateOptions().setUpsert(true)
                mongoClient.updateCollectionWithOptions("score", query, update, options)
                { res: AsyncResult<MongoClientUpdateResult?> ->
                    if (res.succeeded()) {
                        logger.info("Score updated!")
                    } else {
                        res.cause().printStackTrace()
                    }
                }
            }
        }
        vertx.eventBus().localConsumer<String>(STATISTIC_USER).handler {
            val message = JsonObject(it.body())
            logger.info("New message for statistic score: $message")
            val query = JsonObject()
                    .put("puzzleid", message.getValue("puzzleid"))
                    .put("playerid", message.getValue("playerid"))
            launch {
                mongoClient.findAwait("score", query)
                val res = mongoClient.findAwait("score", query)
                it.reply(JsonObject().put("statistic", res).encodePrettily())
            }
        }
    }
}
