package ru.sokolova.englishtelegrambot.telegram.api.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)