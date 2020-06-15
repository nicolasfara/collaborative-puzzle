package it.unibo.pcd.puzzleservice

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PuzzleServiceController {

    @GetMapping("/")
    suspend fun entryPoint(model: Model): String {
        model["title"] = "Puzzle"
        return "puzzle"
    }
}