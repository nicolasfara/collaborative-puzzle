package it.unibo.pcd.playerservice.verticles

import it.unibo.pcd.playerservice.verticles.PlayerVerticle.Const.NEW_USER_QUEUE
import it.unibo.pcd.playerservice.verticles.PlayerVerticle.Const.NEW_USER_RES_QUEUE
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.MessageProperties
import com.viartemev.thewhiterabbit.channel.channel
import com.viartemev.thewhiterabbit.channel.confirmChannel
import com.viartemev.thewhiterabbit.channel.consume
import com.viartemev.thewhiterabbit.channel.publish
import com.viartemev.thewhiterabbit.publisher.OutboundMessage
import com.viartemev.thewhiterabbit.queue.QueueSpecification
import com.viartemev.thewhiterabbit.queue.declareQueue
import io.github.serpro69.kfaker.Faker
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PlayerVerticle : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(PlayerVerticle::class.java)
    private val connection: Connection = ConnectionFactory().newConnection()
    private val faker = Faker()
    private val configMongo = mapOf(Pair("db_name", "Player"),
            Pair("connection_string", "mongodb://localhost:27017"))
    private lateinit var mongoClient: MongoClient

    object Const {
        const val NEW_USER_QUEUE = "user.new"
        const val NEW_USER_RES_QUEUE = "user.new.result"
    }

    override suspend fun start() {
        logger.info("${PlayerVerticle::class.java} started")
        mongoClient = MongoClient.create(vertx, JsonObject(configMongo))

        connection.channel {
            declareQueue(QueueSpecification(NEW_USER_QUEUE, durable = true))
            declareQueue(QueueSpecification(NEW_USER_RES_QUEUE, durable = true))
        }

        launch(vertx.dispatcher()) {
            connection.channel {
                consume(NEW_USER_QUEUE) {
                    consumeMessageWithConfirm {
                        logger.info("[Player-Service]Receive message from Puzzle Service")
                    }
                }
            }
            connection.confirmChannel {
                publish {
                    val playerName = faker.address.city()
                    saveOnMongo(playerName)
                    val playerMessage = JsonObject().put("player-name", playerName)
                    logger.info("[Player-Service] Publish player-name: $playerName")
                    publishWithConfirmAsync(coroutineContext, listOf(OutboundMessage("", NEW_USER_RES_QUEUE,
                            MessageProperties.PERSISTENT_BASIC, playerMessage.encodePrettily())))
                }
            }

        }
    }

    private fun saveOnMongo(playerName: String) {
        val document: JsonObject = JsonObject().put("player-name", playerName)
        mongoClient.save("player", document) { res ->
            if (res.succeeded()) {
                val id = res.result()
                logger.info("[Player-Service] Saved player with id $id")
            } else {
                res.cause().printStackTrace()
            }
        }
    }
}
