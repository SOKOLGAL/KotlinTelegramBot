package ru.sokolova.englishtelegrambot.trainer.modal

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)