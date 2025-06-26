package ru.sokolova.englishtelegrambot.trainer.modal

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val original: String,
    val translate: String,
    val transcription: String,
    var correctAnswersCount: Int = 0,
)