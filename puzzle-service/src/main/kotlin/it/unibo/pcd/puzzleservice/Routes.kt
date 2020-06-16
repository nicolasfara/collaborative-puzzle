package it.unibo.pcd.puzzleservice

import io.vertx.core.Context
import io.vertx.core.MultiMap
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.rabbitmq.basicPublishAwait
import io.vertx.rabbitmq.RabbitMQClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory


class Routes(private val ctx: Context, private val rabbitMQClient: RabbitMQClient) {
    private val logger = LoggerFactory.getLogger("Routes")

    companion object {
        private const val EXCHANGE_NAME = "collaborative.puzzle"
    }

    fun entryPoint(routingContext: RoutingContext) = baseHandler(routingContext) { _, _ ->
        val returnMessage = JsonObject().put("status", "ok")
        routingContext
                .response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(returnMessage))

    }

    fun createPuzzle(routingContext: RoutingContext) = baseHandler(routingContext) { params, coroutineScope ->
        val args = object {
            val imageUrl = params["image-url"]
            val rows = params["rows"]
            val cols = params["cols"]
            val playerId = params["player-id"]
        }

        val message = JsonObject().put("body", Json.encodePrettily(JsonObject.mapFrom(args)))

        coroutineScope.launch {
            rabbitMQClient.basicPublishAwait(EXCHANGE_NAME, "puzzle.new", message)
        }

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(message))
    }

    fun joinPuzzle(routingContext: RoutingContext) = baseHandler(routingContext) { params, _ ->
        val args = object {
            val puzzleId = params["puzzle-id"]
            val playerId = params["player-id"]
        }
    }

    fun leavePuzzle(routingContext: RoutingContext) = baseHandler(routingContext) { params, _ ->
        val args = object {
            val puzzleId = params["puzzle-id"]
            val playerId = params["player-id"]
        }
    }

    fun move(routingContext: RoutingContext) = baseHandler(routingContext) { params, _ ->
        val args = object {
            val puzzleId = params["puzzle-id"]
            val playerId = params["player-id"]
            val originCard = params["origin"]
            val destinationCard = params["destination"]
        }
    }

    fun score(routingContext: RoutingContext) = baseHandler(routingContext) { params, _ ->
        val args = object {
            val playerId = params["player-id"]
        }
    }

    private fun baseHandler(routingContext: RoutingContext, handler: (params: MultiMap, scope: CoroutineScope) -> Unit) {
        GlobalScope.launch(ctx.dispatcher()) {
            val params = routingContext.request().params()
            handler(params, this)
        }
    }
}