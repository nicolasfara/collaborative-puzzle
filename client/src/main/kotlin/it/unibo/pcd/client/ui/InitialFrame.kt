package it.unibo.pcd.client.ui

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import it.unibo.pcd.client.controller.ViewController
import it.unibo.pcd.client.verticles.NetManagerVerticle
import kotlinx.coroutines.Runnable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.*

class InitialFrame(private val controller: ViewController, private val vertx: Vertx) : JFrame() {

    val logger: Logger = LoggerFactory.getLogger("Application")
    private val newPuzzleButton = JButton()
    private val newPuzzle = JPanel()
    private val imageUrlLabel = JLabel()
    private val imageUrlText = JTextField(100)
    private val rowLabel = JLabel()
    private val rowText = JTextField(10)
    private val colLabel = JLabel()
    private val colText = JTextField(10)
    private val puzzleIdLabel = JLabel()
    private val joinPuzzle = JPanel()
    private val joinPuzzleButton = JButton()
    private val puzzleIdText = JTextField(15)

    init {
        title = "Collaborative puzzle"
        isResizable = false
        defaultCloseOperation = EXIT_ON_CLOSE

        newPuzzleButton.text = "Create Puzzle"
        imageUrlLabel.text = "Image URL:"
        rowLabel.text = "Puzzle Row:"
        colLabel.text = "Puzzle Column:"
        newPuzzle.layout = GridLayout(5, 1)
        newPuzzle.add(imageUrlLabel)
        newPuzzle.add(imageUrlText)
        newPuzzle.add(rowLabel)
        newPuzzle.add(rowText)
        newPuzzle.add(colLabel)
        newPuzzle.add(colText)
        newPuzzle.add(newPuzzleButton)
        newPuzzleButton.addActionListener {
            val urlImage = imageUrlText.text
            val rowImage = rowText.text
            val colImage = colText.text
            val message = JsonObject()
                    .put("imageurl", urlImage)
                    .put("rows", rowImage)
                    .put("cols", colImage)

            controller.notifyStarted(message)
        }


        puzzleIdLabel.text = "Puzzle ID:"
        joinPuzzleButton.text = "Join Puzzle"
        joinPuzzle.layout = GridLayout(5, 2)
        joinPuzzle.add(puzzleIdLabel)
        joinPuzzle.add(puzzleIdText)
        joinPuzzle.add(joinPuzzleButton)
        joinPuzzle.border = BorderFactory.createLineBorder(Color.gray)
        joinPuzzleButton.addActionListener {
            val puzzleid = puzzleIdText.text
            val message = JsonObject().put("puzzleid", puzzleid)
            controller.notifyJoined(message)

        }
        contentPane.add(newPuzzle, BorderLayout.NORTH)
        contentPane.add(joinPuzzle, BorderLayout.SOUTH)

    }

    fun createPuzzleBoard(result: JsonObject) {
        val puzzle = PuzzleBoard(rowText.text.toInt(), colText.text.toInt(), imageUrlText.text,
                result.getString("playerid"),
                result.getString("puzzleid"),
                result.getJsonArray("state"),
                controller
        )
        SwingUtilities.invokeLater(Runnable {

            puzzle.isVisible = true
            dispose()
        })
        deployNet(puzzle)
    }

    fun joinPuzzleBoard(result: JsonObject) {
        val puzzle = PuzzleBoard(
                result.getString("rows").toInt(),
                result.getString("cols").toInt(),
                result.getString("imageurl"),
                result.getString("playerid"),
                result.getString("_id"),
                result.getJsonArray("state"),
                controller
        )
        SwingUtilities.invokeLater(Runnable {
            puzzle.isVisible = true
            dispose()
        })
        deployNet(puzzle)
    }

    private fun deployNet(puzzle: PuzzleBoard) {
        val ver = NetManagerVerticle(puzzle, puzzleIdText.text)
        vertx.deployVerticle(ver)
    }
}