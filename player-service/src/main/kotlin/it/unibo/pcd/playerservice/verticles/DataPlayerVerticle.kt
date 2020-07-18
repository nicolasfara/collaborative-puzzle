package it.unibo.pcd.playerservice.verticles

import Constant.NEW_PLAYER
import io.github.cdimascio.dotenv.dotenv
import io.github.serpro69.kfaker.Faker
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.mongo.insertAwait
import kotlinx.coroutines.launch
import java.time.Instant

class DataPlayerVerticle : CoroutineVerticle() {
    private lateinit var mongoClient: MongoClient
    private val faker = Faker()
    private val dotenv = dotenv()
    private val logger = LoggerFactory.getLogger("DataPlayerVerticle")

    override suspend fun start() {
        val mongoConfig = JsonObject().put("db_name", "player")
                .put("connection_string", dotenv["MONGODB_URI"])
        mongoClient = MongoClient.create(vertx, mongoConfig)

        vertx.eventBus().localConsumer<String>(NEW_PLAYER).handler {
            val playerid = generateFakeName()
            val payload = JsonObject().put("playerid", playerid).put("created_at", Instant.now())
            logger.info("New message for new player: $payload")
            launch {
                mongoClient.insertAwait("player", payload)
                val responseBody = JsonObject().put("playerid", playerid)

                it.reply(responseBody.encode())
            }
        }
    }

    /**
     * Generate name based of City in the world.
     */
    private fun generateFakeName(): String {
        return faker.address.city().replace("\\s".toRegex(), "")
    }
}
