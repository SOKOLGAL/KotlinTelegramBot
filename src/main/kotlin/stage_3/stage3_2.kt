package org.example.stage_3

import org.example.stage_2.Word
import java.io.File

fun main() {

    val greetings = """
        Меню:
        1 - Учить слова
        2 - Статистика
        0 - Выход
    """.trimIndent()
    val dictionary: MutableList<Word> = loadDictionary1()

    while (true) {
        println(greetings)
        val enterMenuItemNumber = readlnOrNull()?.toIntOrNull()
        when (enterMenuItemNumber) {
            1 -> println("Выбран пункт \"Учить слова\"")
            2 -> dictionary.filter()
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }

}

fun MutableList<Word>.filter() {
    val totalCount = this.size
    var learnedCount = 0
    this.forEach {
        if (it.correctAnswersCount!! >= WORD_LEARNED) {
            learnedCount++
        }
    }
    val percent = learnedCount * PERCENT / totalCount
    println("Выучено $learnedCount из $totalCount | $percent")
    println()
}

fun loadDictionary1(): MutableList<Word> {
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

const val WORD_LEARNED = 3
const val PERCENT = 100