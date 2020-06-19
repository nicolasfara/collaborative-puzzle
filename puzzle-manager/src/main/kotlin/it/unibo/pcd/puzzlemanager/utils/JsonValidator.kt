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
        isValid = isValid && requestJson.containsKey("player-id")
        isValid = isValid && requestJson.containsKey("image-url")
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
        isValid = isValid && requestJson.containsKey("player-id")
        isValid = isValid && requestJson.containsKey("puzzle-id")
        return isValid
    }

    fun validateLeavePuzzle(request: JsonObject): Boolean {
        var isValid = true
        isValid = isValid && request.containsKey("puzzle-id")
        isValid = isValid && request.containsKey("player-id")
        return isValid
    }

    fun validateSwapPuzzle(request: JsonObject): Boolean {
        var isValid = true
        isValid = isValid && request.containsKey("puzzle-id")
        isValid = isValid && request.containsKey("player-id")
        isValid = isValid && request.containsKey("source")
        isValid = isValid && request.containsKey("destination")
        return isValid
    }
}