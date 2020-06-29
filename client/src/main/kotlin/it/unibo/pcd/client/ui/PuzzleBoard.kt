package it.unibo.pcd.client.ui


import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import it.unibo.pcd.client.controller.ViewController
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.image.BufferedImage
import java.awt.image.CropImageFilter
import java.awt.image.FilteredImageSource
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.function.Consumer
import java.util.function.IntFunction
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.imageio.ImageIO
import javax.swing.*


class PuzzleBoard(private val rows: Int,
                  private val columns: Int,
                  private val imagePath: String,
                  private val playerid: String,
                  private val puzzleid: String,
                  private val state: JsonArray,
                  private val controller: ViewController) : JFrame() {

    private var tiles: MutableList<Tile> = ArrayList()
    private val info = JPanel()
    private val playerIdLabel = JLabel()
    private val puzzleIdLabel = JLabel()
    private val playerId = JLabel()
    private val puzzleId = JLabel()
    private val selectionManager: SelectionManager = SelectionManager(controller)
    val board = JPanel()
    private var currentState = mutableListOf<Int>()

    init {
        title = "Puzzle"
        isResizable = false
        defaultCloseOperation = EXIT_ON_CLOSE

        contentPane.add(board, BorderLayout.CENTER)
        info.layout = GridLayout(2, 2)
        playerIdLabel.text = "PlayerID:"
        playerId.text = playerid
        puzzleIdLabel.text = "PuzzleID:"
        puzzleId.text = puzzleid
        info.add(playerIdLabel)
        info.add(playerId)
        info.add(puzzleIdLabel)
        info.add(puzzleId)

        contentPane.add(info, BorderLayout.SOUTH)
        createTiles(imagePath)
        board.border = BorderFactory.createLineBorder(Color.gray)
        board.layout = GridLayout(rows, columns, 0, 0)
        paintPuzzle(board)
    }

    @Throws(IOException::class)
    private fun createTiles(imagePath: String) {
        val url = URL(imagePath)
        val conn = url.openConnection()
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        conn.connect()
        val urlStream = conn.getInputStream()
        val image: BufferedImage
        image = try {
            ImageIO.read(url)
        } catch (ex: IOException) {
            JOptionPane.showMessageDialog(this,
                    "Could not load image", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        val imageWidth = image.getWidth(null)
        val imageHeight = image.getHeight(null)
        var position = 0
        val randomPositions = IntStream.range(0, state.size())
                .mapToObj(IntFunction<Any> { i: Int ->
                    state.getInteger(i)
                }).collect(Collectors.toList<Any>())
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                val imagePortion = createImage(FilteredImageSource(image.source,
                        CropImageFilter(j * imageWidth / columns,
                                i * imageHeight / rows,
                                imageWidth / columns,
                                imageHeight / rows)))

                tiles.add(Tile(imagePortion, position, randomPositions[position] as Int))
                position++
            }
        }

        val copy: MutableList<Tile> = ArrayList()
        for (i in 0 until tiles.size) {
            copy.add(i, tiles[state.getInteger(i)])
        }
        tiles = copy
        for (i in 0 until state.size()) {
            currentState.add(i, state.getInteger(i))
        }

        for (i in 0 until tiles.size) {
            tiles[i].currentPosition = currentState[i]
        }
    }

    fun paintPuzzle(board: JPanel) {
        var selectionActive = false
        var selectedTile: Tile? = null

        board.removeAll()
        tiles.forEach(Consumer { tile: Tile? ->
            val btn = TileButton(tile!!)
            board.add(btn)
            btn.border = BorderFactory.createLineBorder(Color.gray)
            btn.addActionListener {
                if (selectionActive) {
                    selectionActive = false
                    val message = JsonObject()
                            .put("playerid", playerId.text)
                            .put("puzzleid", puzzleId.text)
                            .put("source", selectedTile!!.currentPosition.toString())
                            .put("destination", tile.currentPosition.toString())

                    swapTile(selectedTile, tile)
                    paintPuzzle(board)
                    controller.swap(message)
                } else {
                    selectionActive = true
                    selectedTile = tile
                }
            }
        })
        pack()
        setLocationRelativeTo(null)
    }

    fun swapTile(t1: Tile?, t2: Tile) {

        val pos = t1!!.currentPosition
        t1.currentPosition = (t2.currentPosition)
        t2.currentPosition = pos
        val first = t1.currentPosition
        val second = t2.currentPosition
        val tmp = first
        currentState[currentState.indexOf(first)] = second
        currentState[currentState.indexOf(second)] = tmp
        tiles.sort()
    }


    fun repaintPuzzle(newState: JsonObject) {

        SwingUtilities.invokeLater {
            val state = newState.getJsonArray("state")
            val newState = mutableListOf<Int>()
            for (i in 0 until state.size()) {
                newState.add(state.getInteger(i))
            }

            val difference = mutableListOf<Int>()
            for (i in 0 until currentState.size) {
                if (currentState[i] != newState[i]) {
                    difference.add(currentState[i])
                }
            }
            if (difference.isNotEmpty() || !difference.isNullOrEmpty()) {
                currentState = newState
                selectionManager.swapTile(tiles[difference[0]], tiles[difference[1]])
                paintPuzzle(board)
            }
        }
    }
}