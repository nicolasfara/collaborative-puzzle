package it.unibo.pcd.puzzlemanager

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
}