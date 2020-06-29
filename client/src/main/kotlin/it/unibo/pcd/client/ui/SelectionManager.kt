package it.unibo.pcd.client.ui


import io.vertx.core.json.JsonObject
import it.unibo.pcd.client.controller.ViewController
import java.io.IOException

class SelectionManager(private val controller: ViewController) {
    private var selectionActive = false
    private var selectedTile: Tile? = null


    @Throws(IOException::class, InterruptedException::class)
    fun selectTile(tile: Tile, puzzleID : String, playerID: String,
                   listener: ()-> Unit) {
        if (selectionActive) {
            selectionActive = false
            val message = JsonObject()
                    .put("playerid", playerID)
                    .put("puzzleid", puzzleID)
                    .put("source", selectedTile!!.currentPosition.toString())
                    .put("destination", tile.currentPosition.toString())

            controller.swap(message)
            swapTile(selectedTile, tile)

        } else {
            selectionActive = true
            selectedTile = tile
        }
    }

    fun swapTile(t1: Tile?, t2: Tile) {
        val pos = t1!!.currentPosition
        t1.currentPosition = (t2.currentPosition)
        t2.currentPosition = pos
    }


}