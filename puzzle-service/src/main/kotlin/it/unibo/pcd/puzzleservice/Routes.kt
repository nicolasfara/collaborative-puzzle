package it.unibo.pcd.puzzleservice

import com.rabbitmq.client.Connection
import com.viartemev.thewhiterabbit.channel.channel
import com.viartemev.thewhiterabbit.channel.confirmChannel
import com.viartemev.thewhiterabbit.channel.consume
import com.viartemev.thewhiterabbit.channel.publish
import io.vertx.core.Context
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.pcd.puzzleservice.util.Constants.NEW_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_USER_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_USER_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Utils
import it.unibo.pcd.puzzleservice.util.Utils.createMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import kotlin.coroutines.coroutineContext


class Routes(private val ctx: Context, private val rabbitConnection: Connection) {
    private val logger = LoggerFactory.getLogger("Routes")

    suspend fun entryPoint(routingContext: RoutingContext) {
        val returnMessage = JsonObject().put("status", "ok")
        routingContext
                .response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(returnMessage))

    }

    suspend fun createPuzzle(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val imageurl = params["imageurl"]
            val rows = params["rows"]
            val cols = params["cols"]
        }
        rabbitConnection.confirmChannel {
            publish {
                publishWithConfirmAsync(coroutineContext, createMessage(NEW_USER_QUEUE, ""))
            }
        }
        var playerId = ""
        rabbitConnection.channel {
            consume(NEW_USER_RES_QUEUE) {
                withContext(ctx.dispatcher()) {
                    consumeMessageWithConfirm {
                        playerId = JsonObject(String(it.body)).getString("player-id")
                        logger.info("Response to create user: $playerId")
                    }
                }
            }
        }

        val newPuzzleMessage = JsonObject.mapFrom(args).put("player-id", playerId)

        rabbitConnection.confirmChannel {
            publish {
                publishWithConfirmAsync(coroutineContext, createMessage(NEW_PUZZLE_QUEUE, newPuzzleMessage.encodePrettily()))
            }
        }
        rabbitConnection.channel {
            consume(NEW_PUZZLE_RES_QUEUE) {
                withContext(ctx.dispatcher()) {
                    consumeMessageWithConfirm {
                        logger.info("New puzzle create successfully")
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(String(it.body))
                    }
                }
            }
        }
    }

    suspend fun joinPuzzle(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val puzzleId = params["puzzle_id"]

        val player = Utils.getRandomString(8)
        val newPlayerMessage = JsonObject().put("body", player)
        val joinPuzzleMessage = JsonObject().put("body", puzzleId)

        // TODO("Generate random user name and give back")
        /*coroutineScope.launch(ctx.dispatcher()) {
            rabbitMQClient.basicPublishAwait(EXCHANGE_NAME, "player.new", newPlayerMessage)
            rabbitMQClient.basicPublishAwait(EXCHANGE_NAME, "puzzle.join", joinPuzzleMessage)
            val joinResult = rabbitMQClient.basicConsumerAwait("puzzle")
            joinResult.handler {
                val res = JsonObject(it.body())
                logger.info("Puzzle manager respond with: $res from ${it.envelope().routingKey()}")
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(Json.encodePrettily(res))
            }
        }*/
    }

    suspend fun leavePuzzle(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val puzzleId = params["puzzle-id"]
            val playerId = params["player-id"]
        }
    }

    suspend fun move(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val puzzleId = params["puzzle-id"]
            val playerId = params["player-id"]
            val originCard = params["origin"]
            val destinationCard = params["destination"]
        }
    }

    suspend fun score(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val playerId = params["player-id"]
        }
    }
}
