package it.unibo.pcd.puzzlemanager

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.pcd.puzzlemanager.utils.Constants
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_JOIN_ADDRESS
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_LEAVE_ADDRESS
import it.unibo.pcd.puzzlemanager.utils.Constants.PUZZLE_SWAP_ADDRESS
import it.unibo.pcd.puzzlemanager.utils.JsonValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class RoutesManager(private val ctx: Context,
                    private val vertx: Vertx) {

    private val validator = JsonValidator()
    private val logger = LoggerFactory.getLogger("RouterManager")
    suspend fun newPuzzle(routingContext: RoutingContext) {

        logger.info("NewPuzzle")
        routingContext.request().bodyHandler {
            val request = it.toJsonObject()
            if (validator.validateNewPuzzle(request)) {
                CoroutineScope(ctx.dispatcher()).launch {
                    val reply = vertx.eventBus().requestAwait<String>(Constants.PUZZLE_NEW_ADDRESS, request.encodePrettily())
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(reply.body())
                }
            }
        }
    }

    suspend fun joinPuzzle(routingContext: RoutingContext) {
        routingContext.request().bodyHandler {
            val request = it.toJsonObject()
            if (validator.validateJoinPuzzle(request)) {
                CoroutineScope(ctx.dispatcher()).launch {
                    val reply = vertx.eventBus().requestAwait<String>(PUZZLE_JOIN_ADDRESS, request.encodePrettily())
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(reply.body())
                }
            }
        }

    }

    suspend fun leavePuzzle(routingContext: RoutingContext) {

        routingContext.request().bodyHandler {
            val request = it.toJsonObject()
            if (validator.validateLeavePuzzle(request)) {
                CoroutineScope(ctx.dispatcher()).launch {
                    val reply = vertx.eventBus().requestAwait<String>(PUZZLE_LEAVE_ADDRESS, request.encodePrettily())
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(reply.body())
                }
            }
        }
    }

    suspend fun swap(routingContext: RoutingContext) {
        routingContext.request().bodyHandler {
            val request = it.toJsonObject()
            if (validator.validateSwapPuzzle(request)) {
                CoroutineScope(ctx.dispatcher()).launch {
                    val reply = vertx.eventBus().requestAwait<String>(PUZZLE_SWAP_ADDRESS, request.encodePrettily())
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(reply.body())
                }
            }
        }
    }
}