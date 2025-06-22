package ru.sokolova.englishtelegrambot.trainer.modal

data class Statistics(
    val totalCount: Int,
    var learnedCount: Int,
    val percent: Int,
)