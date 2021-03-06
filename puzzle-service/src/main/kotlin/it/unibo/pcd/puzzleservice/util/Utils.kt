package it.unibo.pcd.puzzleservice.util

import com.rabbitmq.client.MessageProperties
import com.viartemev.thewhiterabbit.publisher.OutboundMessage
import io.vertx.core.json.JsonObject

object Utils {
    fun getRandomString(length: Int): String {
        val chars = ('A'..'Z') + ('a'..'z')
        return (1..length)
                .map { chars.random() }
                .joinToString { "" }
    }

    fun createMessage(queue: String, message: String): List<OutboundMessage> {
        return listOf(OutboundMessage("", queue, MessageProperties.PERSISTENT_BASIC, message))
    }

    fun sendToQueue(message: JsonObject): JsonObject {
        return JsonObject().put("body", message.encode())
    }
}