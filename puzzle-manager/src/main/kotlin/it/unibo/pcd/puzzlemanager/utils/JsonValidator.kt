package it.unibo.pcd.puzzlemanager.utils

import io.vertx.core.json.JsonObject

class JsonValidator {
    /**
     * A valid input json is like the follow:
     * {
     *   "player-id": "<NAME>",
     *   "image-url": "<IMAGE_URL>",
     *   "rows": "<ROWS>",
     *   "cols": "<COLS>"
     * }
     */
    fun validateNewPuzzle(requestJson: JsonObject): Boolean {
        var isValid = true
        isValid = isValid && requestJson.containsKey("playerid")
        isValid = isValid && requestJson.containsKey("imageurl")
        isValid = isValid && requestJson.containsKey("rows")
        isValid = isValid && requestJson.containsKey("cols")
        return isValid
    }

    /**
     * A valid input json is like the following:
     * {
     *   "player-id": "<PLAYER>",
     *   "puzzle-id": "<PUZZLE_ID>"
     * }
     * Return true if match the above structure, false otherwise.
     */
    fun validateJoinPuzzle(requestJson: JsonObject): Boolean {
        var isValid = true
        isValid = isValid && requestJson.containsKey("playerid")
        isValid = isValid && requestJson.containsKey("puzzleid")
        return isValid
    }

    fun validateLeavePuzzle(request: JsonObject): Boolean {
        var isValid = true
        isValid = isValid && request.containsKey("puzzleid")
        isValid = isValid && request.containsKey("playerid")
        return isValid
    }

    fun validateSwapPuzzle(request: JsonObject): Boolean {
        var isValid = true
        isValid = isValid && request.containsKey("puzzleid")
        isValid = isValid && request.containsKey("playerid")
        isValid = isValid && request.containsKey("source")
        isValid = isValid && request.containsKey("destination")
        return isValid
    }
}