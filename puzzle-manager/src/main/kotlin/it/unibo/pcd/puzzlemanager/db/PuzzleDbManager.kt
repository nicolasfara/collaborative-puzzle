package it.unibo.pcd.puzzlemanager.db

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.ext.mongo.findAwait
import io.vertx.kotlin.ext.mongo.insertAwait
import io.vertx.kotlin.ext.mongo.updateCollectionAwait
import java.util.*

class PuzzleDbManager(private val mongoClient: MongoClient) {

    suspend fun createNewPuzzle(params: JsonObject): String? {
        val playerid = params.getString("playerid")
        val rows = params.getString("rows").toInt()
        val cols = params.getString("cols").toInt()
        val puzzleState = JsonArray()
        (0.until(rows*cols)).shuffled().forEach { puzzleState.add(it) } //Shuffle all puzzle cells

        val document = params.copy()
        document.remove("playerid")
        document.put("players", JsonArray().add(playerid))
                .put("create_by", playerid)
                .put("state", puzzleState)
                .put("complete", false)

        return mongoClient.insertAwait("puzzle", document)
    }

    suspend fun joinPuzzle(params: JsonObject, newPlayer: String): Optional<JsonObject> {
        val updateParams = JsonObject().put("\$push", JsonObject().put("players", newPlayer))
        val queryRes = mongoClient.updateCollectionAwait("puzzle", params, updateParams)
        return if (queryRes!!.docMatched > 0) {
            Optional.of(mongoClient.findAwait("puzzle", params)[0].put("playerid", newPlayer))
        } else {
            Optional.empty()
        }
    }

    suspend fun leavePuzzle(params: JsonObject, exitPlayer: String): Optional<JsonObject> {
        val updateParams = JsonObject().put("\$pull", JsonObject().put("players", exitPlayer))
        val queryRes = mongoClient.updateCollectionAwait("puzzle", params, updateParams)
        return if (queryRes!!.docMatched > 0) {
            Optional.of(mongoClient.findAwait("puzzle", params)[0])
        } else {
            Optional.empty()
        }
    }

    suspend fun swapPuzzle(params: JsonObject, source: Int, destination: Int): Optional<JsonObject> {
        val puzzle = mongoClient.findAwait("puzzle", params)[0]
        val playersList = puzzle.getJsonArray("state").toMutableList()
        println("list: $playersList")
        Collections.swap(playersList, playersList.indexOf(source), playersList.indexOf(destination))
        val updateParams = JsonObject().put("\$set", JsonObject().put("state", playersList))
        val res = mongoClient.updateCollectionAwait("puzzle", params, updateParams)
        return if (res!!.docMatched > 0) {
            Optional.of(mongoClient.findAwait("puzzle", params)[0])
        } else {
            Optional.empty()
        }
    }

    suspend fun isUserInPuzzle(playerid: String, puzzleid: String): Boolean {
        val params = JsonObject().put("players", playerid).put("_id", puzzleid)
        val playerList = mongoClient.findAwait("puzzle", params)
        println(playerList)
        return playerList.isNotEmpty()
    }
}