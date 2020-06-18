package it.unibo.pcd.puzzlemanager

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.ext.mongo.insertAwait

class PuzzleDbManager(private val mongoClient: MongoClient) {

    suspend fun createNewPuzzle(params: JsonObject): String? {
        val playerId = params.getString("player-id")
        val rows = params.getString("rows").toInt()
        val cols = params.getString("cols").toInt()
        val puzzleState = JsonArray()
        (0.until(rows*cols)).shuffled().forEach { puzzleState.add(it) } //Shuffle all puzzle cells

        val document = params
                .put("players", JsonArray().add(playerId))
                .put("state", puzzleState)
                .put("complete", false)

        return mongoClient.insertAwait("puzzle", document)
    }
}