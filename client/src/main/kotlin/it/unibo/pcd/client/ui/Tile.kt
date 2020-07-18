package it.unibo.pcd.client.ui

import java.awt.Image
class Tile(val image: Image,
           private val originalPosition: Int,
           var currentPosition: Int) : Comparable<Tile> {

    val isInRightPlace: Boolean
        get() = currentPosition == originalPosition

    override operator fun compareTo(other: Tile): Int {
        return currentPosition.compareTo(other.currentPosition)
    }
}