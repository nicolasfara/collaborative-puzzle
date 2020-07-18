package it.unibo.pcd.playerservice

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.eventbus.requestAwait
import org.slf4j.LoggerFactory

class RouterManager(private val vertx: Vertx) {
    private val logger = LoggerFactory.getLogger("RouterManager")

    suspend fun createNewUser(routingContext: RoutingContext) {
        logger.info("New user request")
        val newUserRes = vertx.eventBus().requestAwait<String>(Constant.NEW_PLAYER, "")
        val newUserJson = JsonObject(newUserRes.body())
        logger.info("Create new user: $newUserJson")
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(newUserJson.encode())
    }
}