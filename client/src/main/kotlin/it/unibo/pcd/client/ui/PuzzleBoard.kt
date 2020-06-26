package it.unibo.pcd.client.ui


import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import it.unibo.pcd.client.controller.ViewController
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
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

    private val tiles: MutableList<Tile> = ArrayList()
    private val info = JPanel()
    private val playerIdLabel = JLabel()
    private val puzzleIdLabel = JLabel()
    private val playerId = JLabel()
    private val puzzleId = JLabel()
    private val selectionManager: SelectionManager = SelectionManager(controller)
    private val board = JPanel()

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
        paintPuzzle()
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
    }

    fun paintPuzzle() {
        SwingUtilities.invokeLater(kotlinx.coroutines.Runnable {
            board.removeAll()
            tiles.sort()
            tiles.forEach(Consumer { tile: Tile ->
                val btn = TileButton(tile)
                board.add(btn)
                btn.border = BorderFactory.createLineBorder(Color.gray)
                btn.addActionListener {
                    try {
                        selectionManager.selectTile(tile, puzzleId.text, playerId.text) {
                            paintPuzzle()
                            //checkSolution()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            })
            pack()
            setLocationRelativeTo(null)
        })
    }

    //    private fun checkSolution() {
//        if (tiles.stream().allMatch { obj: Tile -> obj.isInRightPlace() }) {
//            JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE)
//        }
//    }
    fun repaintPuzzle(newState: JsonObject) {
        //TODO
        /*Function call when other player swap puzzle*/
        SwingUtilities.invokeLater(kotlinx.coroutines.Runnable {
            
        })
    }

}