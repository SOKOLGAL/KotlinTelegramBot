package ru.sokolova.englishtelegrambot.telegram.api.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUpdates(
    @SerialName("update_id")
    val updateId: Long,
)