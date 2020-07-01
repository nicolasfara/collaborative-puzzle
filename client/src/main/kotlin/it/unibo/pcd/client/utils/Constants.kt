package it.unibo.pcd.client.utils

object Constants {
    // EventBus addresses
    const val CREATE_ADDRESS = "create.puzzle"
    const val JOIN_ADDRESS = "join.puzzle"
    const val SWAP_ADDRESS = "swap"
    const val UPDATE_STATE_ADDRESS = "update.state"
    const val POINTER_ADDRESS = "pointer"

    // Web client URI
    const val CREATE_URI = "/api/create_puzzle"
    const val JOIN_URI = "/api/join_puzzle"
    const val SWAP_URI = "/api/swap"

    // WebSocket URI
    const val PUZZLE_WS_URI = "/puzzle/"
    const val POINTER_WS_URI = "/pointer/"
}