package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int? = 0,
)

fun main() {

    val greetings = """
        Меню:
        1 - Учить слова
        2 - Статистика
        0 - Выход
    """.trimIndent()
    val dictionary: List<Word> = loadDictionary()

    while (true) {
        println(greetings)
        val enterMenuItemNumber = readlnOrNull()?.toIntOrNull()
        when (enterMenuItemNumber) {
            1 -> println("Выбран пункт \"Учить слова\"")
            2 -> getLearnedWords(dictionary)
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }

}

fun getLearnedWords(dictionary: List<Word>) {
    val totalCount = dictionary.size
    var learnedCount = 0
    dictionary.forEach {
        if ((it.correctAnswersCount ?: 0) >= NUMBER_CORRECT_ANSWERS) {
            learnedCount++
        }
    }
    val percent = learnedCount * PERCENT / totalCount
    println("Выучено $learnedCount из $totalCount | $percent")
    println()
}

fun loadDictionary(): List<Word> {
    val wordsFile: File = File("words.txt")
    val lines = wordsFile.readLines()
    val dictionary: MutableList<Word> = mutableListOf()

    for (line in lines) {
        val line = line.split("|")
        var word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }
    return dictionary
}

const val NUMBER_CORRECT_ANSWERS = 3
const val PERCENT = 100