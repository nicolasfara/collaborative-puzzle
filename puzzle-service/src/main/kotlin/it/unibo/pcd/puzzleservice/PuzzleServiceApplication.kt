package it.unibo.pcd.puzzleservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PuzzleServiceApplication

fun main(args: Array<String>) {
    runApplication<PuzzleServiceApplication>(*args)
}
