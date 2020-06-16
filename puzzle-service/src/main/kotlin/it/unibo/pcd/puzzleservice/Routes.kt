package it.unibo.pcd.puzzleservice

import io.vertx.core.Context
import io.vertx.core.MultiMap
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Routes(private val ctx: Context) {
    fun entryPoint(routingContext: RoutingContext) = baseHandler(routingContext) {
            val json = object {
                val name = it["name"]
                val poppi = it["surname"]
            }
            routingContext
                    .response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(json))
    }

    fun createPuzzle(routingContext: RoutingContext) = baseHandler(routingContext) {
            val args = object {
                val imageUrl = it["image-url"]
                val rows = it["rows"]
                val cols = it["cols"]
                val playerId = it["player-id"]
            }

            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(args))

    }

    fun joinPuzzle(routingContext: RoutingContext) = baseHandler(routingContext) {
        val args = object {
            val puzzleId = it["puzzle-id"]
            val playerId = it["player-id"]
        }
    }

    fun leavePuzzle(routingContext: RoutingContext) = baseHandler(routingContext) {
        val args = object {
            val puzzleId = it["puzzle-id"]
            val playerId = it["player-id"]
        }
    }

    fun move(routingContext: RoutingContext) = baseHandler(routingContext) {
        val args = object {
            val puzzleId = it["puzzle-id"]
            val playerId = it["player-id"]
            val originCard = it["origin"]
            val destinationCard = it["destination"]
        }
    }

    fun score(routingContext: RoutingContext) = baseHandler(routingContext) {
        val args = object {
            val playerId = it["player-id"]
        }
    }

    private fun baseHandler(routingContext: RoutingContext, handler: (params: MultiMap) -> Unit) {
        GlobalScope.launch(ctx.dispatcher()) {
            val params = routingContext.request().params()
            handler(params)
        }
    }
}