package it.unibo.pcd.scoringservice.verticles

import Constant.NEW_SCORE
import Constant.SCORE_QUEUE
import Constant.SCORE_STATISTIC_QUEUE
import Constant.SCORE_STATISTIC_RES_QUEUE
import Constant.STATISTIC_USER
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.rabbitmq.basicConsumerAwait
import io.vertx.kotlin.rabbitmq.basicPublishAwait
import io.vertx.kotlin.rabbitmq.queueDeclareAwait
import io.vertx.kotlin.rabbitmq.startAwait
import io.vertx.rabbitmq.RabbitMQClient
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ScoringVerticle : CoroutineVerticle() {
    private val logger: Logger = LoggerFactory.getLogger(ScoringVerticle::class.java)
    private lateinit var client: RabbitMQClient


    override suspend fun start() {
        logger.info("${ScoringVerticle::class.java} started")
        client = RabbitMQClient.create(vertx, config)
        client.startAwait()

        client.queueDeclareAwait(SCORE_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(SCORE_STATISTIC_QUEUE, durable = true, exclusive = false, autoDelete = false)
        client.queueDeclareAwait(SCORE_STATISTIC_RES_QUEUE, durable = true, exclusive = false, autoDelete = false)

        client.basicConsumerAwait(SCORE_QUEUE).handler {
            logger.info("Receive message from Puzzle Manger")
            val request = JsonObject(it.body())

            launch {
                val reply = awaitResult<Message<String>> { handler ->
                    vertx.eventBus().request(NEW_SCORE, request.encodePrettily(), handler)
                }
                logger.info("Store new score")
            }
        }
        client.basicConsumerAwait(SCORE_STATISTIC_QUEUE).handler {
            logger.info("Receive message from Puzzle service")
            val req = JsonObject(it.body())
            launch {
                val reply = awaitResult<Message<String>> { handler ->
                    vertx.eventBus().request(STATISTIC_USER, req.encodePrettily(), handler)
                }
                logger.info("Statistic response")
                client.basicPublishAwait("", SCORE_STATISTIC_RES_QUEUE, JsonObject(reply.body()))
            }
        }
    }
}
