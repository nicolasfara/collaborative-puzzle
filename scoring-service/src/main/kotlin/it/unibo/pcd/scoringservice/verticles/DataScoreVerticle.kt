package it.unibo.pcd.scoringservice.verticles

import Constant.NEW_SCORE
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.MongoClientUpdateResult
import io.vertx.ext.mongo.UpdateOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle
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
            logger.info("[Scoring-Service]-New message for new score: $message")
            val query = JsonObject()
                    .put("puzzle-id", message.getValue("puzzle-id"))
                    .put("player-id", message.getValue("player-id"))
            val update = JsonObject().put("\$inc", JsonObject()
                    .put("score", message.getValue("score")))

            launch {
                val options: UpdateOptions = UpdateOptions().setUpsert(true)
                mongoClient.updateCollectionWithOptions("Score", query, update, options)
                { res: AsyncResult<MongoClientUpdateResult?> ->
                    if (res.succeeded()) {
                        logger.info("Score updated !")
                    } else {
                        res.cause().printStackTrace()
                    }
                }
            }
        }
    }
}
