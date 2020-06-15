package it.unibo.pcd.puzzleservice.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PuzzleApi {
    @GetMapping("/new-puzzle")
    suspend fun createPuzzle(
            @RequestParam(value = "image") image: String,
            @RequestParam(value = "cols", defaultValue = "5") cols: Int,
            @RequestParam(value = "rows", defaultValue = "5") rows: Int
    ): String {
        return ""
    }
}