package it.unibo.pcd.puzzleservice

import com.rabbitmq.client.Connection
import com.viartemev.thewhiterabbit.channel.*
import io.vertx.core.Context
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.pcd.puzzleservice.util.Constants.JOIN_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.JOIN_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.LEAVE_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.LEAVE_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_USER_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.NEW_USER_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.SWAP_PUZZLE_QUEUE
import it.unibo.pcd.puzzleservice.util.Constants.SWAP_PUZZLE_RES_QUEUE
import it.unibo.pcd.puzzleservice.util.Utils
import it.unibo.pcd.puzzleservice.util.Utils.createMessage
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

    /**
     * Handler for route: /api/create_puzzle. Create a new user and give back the user-id, after create a new puzzle
     * send all the properties like rows, cols, image-url; after all give back to the user a json with:
     * puzzle-id and player-id.
     */
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
        val puzzleId = params["puzzle-id"]

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

       val joinPuzzleMessage = JsonObject().put("puzzle-id", puzzleId).put("player-id", playerId)
        rabbitConnection.confirmChannel{
            publish {
                publishWithConfirmAsync(coroutineContext, createMessage(JOIN_PUZZLE_QUEUE, joinPuzzleMessage.encodePrettily()))
            }
        }

        rabbitConnection.channel {
            consume(JOIN_PUZZLE_RES_QUEUE) {
                withContext(ctx.dispatcher()) {
                    consumeMessageWithConfirm {
                        val status = JsonObject(String(it.body))
                        logger.info("Response from join puzzle: $status")
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(status.encodePrettily())
                    }
                }
            }
        }
    }

    suspend fun leavePuzzle(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val puzzleId = params["puzzle-id"]
            val playerId = params["player-id"]
        }
        rabbitConnection.confirmChannel {
            publish {
                publishWithConfirmAsync(coroutineContext, createMessage(LEAVE_PUZZLE_QUEUE, JsonObject.mapFrom(args).encodePrettily()))
            }
        }

        rabbitConnection.channel {
            consume(LEAVE_PUZZLE_RES_QUEUE) {
                withContext(ctx.dispatcher()) {
                    consumeMessageWithConfirm {
                        val result = JsonObject(String(it.body))
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(result.encodePrettily())
                    }
                }
            }
        }
    }

    suspend fun swap(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val puzzleId = params["puzzle-id"]
            val playerId = params["player-id"]
            val originCard = params["source"]
            val destinationCard = params["destination"]
        }

        rabbitConnection.confirmChannel {
            publish {
                publishWithConfirmAsync(coroutineContext, createMessage(SWAP_PUZZLE_QUEUE, JsonObject.mapFrom(args).encodePrettily()))
            }
        }

        rabbitConnection.channel {
            consume(SWAP_PUZZLE_RES_QUEUE) {
                withContext(ctx.dispatcher()) {
                    consumeMessageWithConfirm {
                        val result = JsonObject(String(it.body))
                        logger.info("Response after swap: $result")
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(result.encodePrettily())
                    }
                }
            }
        }
    }

    suspend fun score(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val playerId = params["player-id"]
        }
    }
}
