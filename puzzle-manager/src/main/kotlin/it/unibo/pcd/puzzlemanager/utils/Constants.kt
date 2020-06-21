package it.unibo.pcd.puzzlemanager.utils

object Constants {
    const val PUZZLE_NEW_QUEUE = "puzzle.new"
    const val PUZZLE_NEW_RES_QUEUE = "puzzle.new.result"
    const val PUZZLE_JOIN_QUEUE = "puzzle.join"
    const val PUZZLE_JOIN_RES_QUEUE = "puzzle.join.result"
    const val PUZZLE_LEAVE_QUEUE = "puzzle.leave"
    const val PUZZLE_LEAVE_RES_QUEUE = "puzzle.leave.result"
    const val PUZZLE_SWAP_QUEUE = "puzzle.swap"
    const val PUZZLE_SWAP_RES_QUEUE = "puzzle.swap.result"

    // Vertx event bus chanel
    const val PUZZLE_NEW_ADDRESS = "puzzle.new"
    const val PUZZLE_JOIN_ADDRESS = "puzzle.join"
    const val PUZZLE_LEAVE_ADDRESS = "puzzle.leave"
    const val PUZZLE_SWAP_ADDRESS = "puzzle.swap"
}