package it.unibo.pcd.puzzleservice

import io.vertx.core.Context
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonObjectAwait
import io.vertx.kotlin.rabbitmq.basicPublishAwait
import io.vertx.rabbitmq.RabbitMQClient
import it.unibo.pcd.puzzleservice.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory


class Routes(private val ctx: Context, private val rabbitMQClient: RabbitMQClient, private val webClient: WebClient) {
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
        routingContext.request().handler {
            CoroutineScope(ctx.dispatcher()).launch {
                val params = it.toJsonObject()
                val args = object {
                    val imageurl: String = params["imageurl"]
                    val rows: String = params["rows"]
                    val cols: String = params["cols"]
                }

                val newUserResult = webClient.get(8081, "player-service", "/api/new_user").sendAwait()
                val userResultJson = newUserResult.bodyAsJsonObject()

                val newPuzzleRequest = JsonObject.mapFrom(args).put("playerid", userResultJson.getString("playerid"))
                val newPuzzleResult = webClient.post(8082, "puzzle-manager", "/api/new_puzzle")
                        .sendJsonObjectAwait(newPuzzleRequest)

                val newPuzzleResultJson = newPuzzleResult.bodyAsJsonObject()
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(newPuzzleResultJson.encode())
            }
        }
    }

    suspend fun joinPuzzle(routingContext: RoutingContext) {
        routingContext.request().handler {
            CoroutineScope(ctx.dispatcher()).launch {
                val params = it.toJsonObject()
                val puzzleid: String = params["puzzleid"]

                val newUserResult = webClient.get(8081, "player-service", "/api/new_user").sendAwait()
                val userResultJson = newUserResult.bodyAsJsonObject()

                val newPuzzleRequest = JsonObject().put("puzzleid", puzzleid).put("playerid", userResultJson.getString("playerid"))
                val joinPuzzleRes = webClient.post(8082, "puzzle-manager", "/api/join_puzzle")
                        .sendJsonObjectAwait(newPuzzleRequest)
                val joinPuzzleResJson = joinPuzzleRes.bodyAsJsonObject()

                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(joinPuzzleResJson.encode())
            }
        }

    }

    suspend fun leavePuzzle(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val puzzleid = params["puzzleid"]
            val playerid = params["playerid"]
        }

        val leaveRequest = JsonObject.mapFrom(args)
        val leaveRes = webClient.post(8082, "localhost", "/api/leave_puzzle")
                .sendJsonObjectAwait(leaveRequest)
        val leaveResJson = leaveRes.bodyAsJsonObject()

        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(leaveResJson.encode())
    }

    suspend fun swap(routingContext: RoutingContext) {
        routingContext.request().handler {
            CoroutineScope(ctx.dispatcher()).launch {
                val params = it.toJsonObject()
                val args = object {
                    val puzzleid: String = params["puzzleid"]
                    val playerid: String = params["playerid"]
                    val source: String = params["source"]
                    val destination: String = params["destination"]
                }

                val swapRequest = JsonObject.mapFrom(args)
                val swapRes = webClient.post(8082, "localhost", "/api/swap")
                        .sendJsonObjectAwait(swapRequest)
                val swapResJson = swapRes.bodyAsJsonObject()
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(swapResJson.encode())
            }
        }
    }

    suspend fun pointerUpdate(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val playerid = params["playerid"]
            val puzzleid = params["puzzleid"]
            val pointer = params["pointer"]
        }
        logger.info("Pointer update request")
        val payload = JsonObject().put("body", JsonObject.mapFrom(args).encode())
        rabbitMQClient.basicPublishAwait(Constants.EXCHANGE_NAME, "pointer.update", payload)
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(JsonObject.mapFrom(args).encode())
    }

    suspend fun score(routingContext: RoutingContext) {
        val params = routingContext.request().params()
        val args = object {
            val playerId = params["playerid"]
        }
    }
}
