package org.example.stage_2

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int? = 0,
)

fun main() {

    val wordsFile: File = File("words.txt")
    val lines = wordsFile.readLines()
    val dictionary: MutableList<Word> = mutableListOf()

    for (line in lines) {
        val line = line.split("|")
        var word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull())
        var correctAnswersCount: Int = word.correctAnswersCount ?: 0
        word = word.copy(correctAnswersCount = correctAnswersCount)
        dictionary.add(word)
    }

    for (i in dictionary) {
        println(i)
    }

}